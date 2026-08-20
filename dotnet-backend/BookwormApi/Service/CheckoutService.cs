using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class CheckoutService : ICheckoutService
{
    private const int MoneyScale = 2;

    private readonly BookwormDbContext _db;
    private readonly IShelfService _shelfService;
    private readonly IMyLibraryService _myLibraryService;
    private readonly IBeneficiaryAssignmentService _beneficiaryAssignmentService;
    private readonly ITransactionPdfService _transactionPdfService;
    private readonly IEmailService _emailService;
    private readonly ILogger<CheckoutService> _logger;

    public CheckoutService(
        BookwormDbContext db,
        IShelfService shelfService,
        IMyLibraryService myLibraryService,
        IBeneficiaryAssignmentService beneficiaryAssignmentService,
        ITransactionPdfService transactionPdfService,
        IEmailService emailService,
        ILogger<CheckoutService> logger)
    {
        _db = db;
        _shelfService = shelfService;
        _myLibraryService = myLibraryService;
        _beneficiaryAssignmentService = beneficiaryAssignmentService;
        _transactionPdfService = transactionPdfService;
        _emailService = emailService;
        _logger = logger;
    }

    public async Task<List<Transaction>> CheckoutAsync(User user)
    {
        var cartLines = await _db.Carts
            .Where(c => c.UserId == user.UserId)
            .Include(c => c.Product)
            .ToListAsync();

        if (cartLines.Count == 0)
        {
            throw new InvalidOperationException("Your cart is empty.");
        }

        var buyLines = cartLines.Where(c => c.RentDays is null).ToList();
        var rentLines = cartLines.Where(c => c.RentDays is not null).ToList();

        var results = new List<Transaction>();

        if (buyLines.Count > 0)
        {
            results.Add(await ProcessTransactionAsync(user, buyLines, TransactionType.BUY));
        }
        if (rentLines.Count > 0)
        {
            results.Add(await ProcessTransactionAsync(user, rentLines, TransactionType.RENT));
        }

        var allCarts = await _db.Carts.Where(c => c.UserId == user.UserId).ToListAsync();
        _db.Carts.RemoveRange(allCarts);
        await _db.SaveChangesAsync();

        return results;
    }

    private async Task<Transaction> ProcessTransactionAsync(User user, List<Cart> lines, TransactionType type)
    {
        var transaction = new Transaction
        {
            UserId = user.UserId,
            TransactionType = type,
            Status = TransactionStatus.PENDING,
            CreatedAt = DateTime.Now
        };
        _db.Transactions.Add(transaction);
        await _db.SaveChangesAsync();

        var today = DateOnly.FromDateTime(DateTime.Today);
        decimal total = 0m;

        foreach (var line in lines)
        {
            var product = line.Product;
            var unitPrice = UnitPrice(product, type, line.RentDays);
            var lineTotal = Math.Round(unitPrice * line.Qty, MoneyScale, MidpointRounding.AwayFromZero);

            var item = new TransactionItem
            {
                TransactionId = transaction.TransactionId,
                ProductId = product.ProductId,
                Price = unitPrice,
                Quantity = line.Qty
            };
            _db.TransactionItems.Add(item);
            await _db.SaveChangesAsync();

            total += lineTotal;

            await RecordRoyaltyAsync(product, item, lineTotal, today);

            if (type == TransactionType.BUY)
            {
                await _shelfService.AddToShelfAsync(user, product);
            }
            else
            {
                await _myLibraryService.RecordRentalAsync(user, product, line.RentDays!.Value);
            }
        }

        transaction.TotalAmount = total;
        transaction.Status = TransactionStatus.SUCCESS;
        await _db.SaveChangesAsync();

        var items = await _db.TransactionItems
            .Where(i => i.TransactionId == transaction.TransactionId)
            .Include(i => i.Product)
            .ToListAsync();

        transaction.User = user;
        var invoice = _transactionPdfService.GenerateInvoice(transaction, items);
        await _emailService.SendTransactionSuccessEmailAsync(user.UserEmail, transaction, invoice);

        return transaction;
    }

    private static decimal UnitPrice(Product product, TransactionType type, int? rentDays)
    {
        if (type == TransactionType.RENT)
        {
            if (!product.Rentable)
            {
                throw new InvalidOperationException($"{product.ProductName} is not available to rent.");
            }
            if (product.RentPerDay is null)
            {
                throw new InvalidOperationException($"No daily rate is set for {product.ProductName}.");
            }
            if (rentDays is null || rentDays < product.MinRentDays)
            {
                throw new ArgumentException($"{product.ProductName} must be rented for at least {product.MinRentDays} days.");
            }
            return Math.Round(product.RentPerDay.Value * rentDays.Value, MoneyScale, MidpointRounding.AwayFromZero);
        }

        var today = DateOnly.FromDateTime(DateTime.Today);
        var offerIsLive = product.ProductOfferprice is not null && product.ProductOffPriceExpirydate is not null &&
                           product.ProductOffPriceExpirydate.Value > today;
        if (offerIsLive)
        {
            return Math.Round(product.ProductOfferprice!.Value, MoneyScale, MidpointRounding.AwayFromZero);
        }

        var hasDiscount = product.DiscountPercent is not null && product.DiscountPercent > 0;
        if (hasDiscount)
        {
            var discount = Math.Round(product.ProductBaseprice * product.DiscountPercent!.Value / 100, MoneyScale, MidpointRounding.AwayFromZero);
            return Math.Round(product.ProductBaseprice - discount, MoneyScale, MidpointRounding.AwayFromZero);
        }

        return Math.Round(product.ProductBaseprice, MoneyScale, MidpointRounding.AwayFromZero);
    }

    private async Task RecordRoyaltyAsync(Product product, TransactionItem item, decimal lineTotal, DateOnly today)
    {
        var royaltyPercent = product.RoyaltyPercent;
        if (royaltyPercent is null || royaltyPercent <= 0)
        {
            return;
        }

        var beneficiaries = await _beneficiaryAssignmentService.GetAssignedBeneficiariesAsync(product);
        if (beneficiaries.Count == 0)
        {
            return;
        }

        var totalRoyalty = Math.Round(lineTotal * royaltyPercent.Value / 100, MoneyScale, MidpointRounding.AwayFromZero);

        var royaltyCalc = new RoyaltyCalculation
        {
            ProductId = product.ProductId,
            ItemId = item.ItemId,
            RoycalTranDate = today,
            TotalAmount = lineTotal,
            RoyaltyPercent = royaltyPercent,
            TotalRoyalty = totalRoyalty
        };
        _db.RoyaltyCalculations.Add(royaltyCalc);
        await _db.SaveChangesAsync();

        var perBeneficiary = Math.Round(totalRoyalty / beneficiaries.Count, MoneyScale, MidpointRounding.AwayFromZero);

        foreach (var beneficiary in beneficiaries)
        {
            _db.ProductBeneficiaries.Add(new ProductBeneficiary
            {
                ProductId = product.ProductId,
                BeneficiaryId = beneficiary.BeneficiaryId,
                RoycalId = royaltyCalc.RoycalId,
                RoyaltyReceived = perBeneficiary
            });
        }
        await _db.SaveChangesAsync();
    }
}
