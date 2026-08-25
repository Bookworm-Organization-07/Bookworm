using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class LibraryCheckoutService : ILibraryCheckoutService
{
    private const int MoneyScale = 2;

    private readonly BookwormDbContext _db;
    private readonly IBeneficiaryAssignmentService _beneficiaryAssignmentService;
    private readonly ILibraryInvoicePdfService _libraryInvoicePdfService;
    private readonly IEmailService _emailService;

    public LibraryCheckoutService(
        BookwormDbContext db,
        IBeneficiaryAssignmentService beneficiaryAssignmentService,
        ILibraryInvoicePdfService libraryInvoicePdfService,
        IEmailService emailService)
    {
        _db = db;
        _beneficiaryAssignmentService = beneficiaryAssignmentService;
        _libraryInvoicePdfService = libraryInvoicePdfService;
        _emailService = emailService;
    }

    public async Task CheckoutAsync(User user, LibraryCheckoutRequest request)
    {
        if (request.ProductIds is null || request.ProductIds.Count == 0)
        {
            throw new ArgumentException("Select at least one title to borrow.");
        }

        var libraryPackage = request.PackageId.HasValue
            ? await _db.LibraryPackages.FirstOrDefaultAsync(p => p.PackageId == request.PackageId.Value)
            : null;
        if (libraryPackage is null)
        {
            throw new ArgumentException("Library package not found.");
        }

        var bookLimit = libraryPackage.BookLimit;
        if (bookLimit <= 0)
        {
            throw new InvalidOperationException($"The {libraryPackage.Name} package allows no books.");
        }

        var products = await _db.Products.Where(p => request.ProductIds.Contains(p.ProductId)).ToListAsync();
        if (products.Count != request.ProductIds.Count)
        {
            throw new ArgumentException("One of the selected titles no longer exists.");
        }

        var today = DateOnly.FromDateTime(DateTime.Today);
        var now = DateTime.Now;

        var activePurchase = await FindActivePurchaseAsync(user, libraryPackage, now);

        var transaction = new Transaction
        {
            UserId = user.UserId,
            TransactionType = TransactionType.LEND,
            Status = TransactionStatus.PENDING,
            CreatedAt = now,
            TotalAmount = activePurchase is not null ? 0m : libraryPackage.Cost
        };
        _db.Transactions.Add(transaction);
        await _db.SaveChangesAsync();

        var avgBookPrice = Math.Round(libraryPackage.Cost / bookLimit, MoneyScale, MidpointRounding.AwayFromZero);

        LibraryPackagePurchase purchase;
        if (activePurchase is not null)
        {
            purchase = activePurchase;
        }
        else
        {
            purchase = new LibraryPackagePurchase
            {
                UserId = user.UserId,
                TransactionId = transaction.TransactionId,
                PackageId = libraryPackage.PackageId,
                PackagePrice = libraryPackage.Cost,
                AllowedBooks = bookLimit,
                AvgBookPrice = avgBookPrice,
                PurchaseDate = now
            };
            _db.LibraryPackagePurchases.Add(purchase);
            await _db.SaveChangesAsync();
        }

        var alreadyBorrowed = await _db.MyLibraries.CountAsync(m =>
            m.UserId == user.UserId && m.AccessType == LibraryAccessType.LEND && m.EndDate != null && m.EndDate > today);

        if (alreadyBorrowed + products.Count > purchase.AllowedBooks)
        {
            throw new InvalidOperationException(
                $"That would take you to {alreadyBorrowed + products.Count} books; the {libraryPackage.Name} package allows {purchase.AllowedBooks}.");
        }

        var endDate = today.AddDays(libraryPackage.ValidityDays);
        var booksTaken = alreadyBorrowed;

        foreach (var product in products)
        {
            if (!product.Library)
            {
                throw new InvalidOperationException($"{product.ProductName} is not available in the lending library.");
            }

            var alreadyOut = await _db.MyLibraries.AnyAsync(m =>
                m.UserId == user.UserId && m.ProductId == product.ProductId &&
                m.AccessType == LibraryAccessType.LEND && m.EndDate != null && m.EndDate > today);
            if (alreadyOut)
            {
                throw new InvalidOperationException($"{product.ProductName} is already in your library.");
            }

            booksTaken++;

            var royaltyPercent = product.RoyaltyPercent ?? 0m;
            var royaltyAmount = Math.Round(purchase.AvgBookPrice * royaltyPercent / 100, MoneyScale, MidpointRounding.AwayFromZero);

            var item = new TransactionItem
            {
                TransactionId = transaction.TransactionId,
                ProductId = product.ProductId,
                Price = purchase.AvgBookPrice,
                Quantity = 1
            };
            _db.TransactionItems.Add(item);
            await _db.SaveChangesAsync();

            _db.LibraryPackagePurchaseItems.Add(new LibraryPackagePurchaseItem
            {
                PurchaseId = purchase.PurchaseId,
                ProductId = product.ProductId,
                RoyaltyPercent = royaltyPercent,
                RoyaltyAmount = royaltyAmount
            });
            await _db.SaveChangesAsync();

            await RecordRoyaltyAsync(product, item, purchase.AvgBookPrice, royaltyPercent, royaltyAmount, today);

            _db.MyLibraries.Add(new MyLibrary
            {
                UserId = user.UserId,
                PackageId = libraryPackage.PackageId,
                ProductId = product.ProductId,
                AccessType = LibraryAccessType.LEND,
                StartDate = today,
                EndDate = endDate,
                BooksAllowed = bookLimit,
                BooksTaken = booksTaken
            });
            await _db.SaveChangesAsync();
        }

        transaction.Status = TransactionStatus.SUCCESS;
        await _db.SaveChangesAsync();

        purchase.User = user;
        purchase.LibraryPackage = libraryPackage;
        purchase.Transaction = transaction;
        var purchaseItems = await _db.LibraryPackagePurchaseItems
            .Where(i => i.PurchaseId == purchase.PurchaseId)
            .Include(i => i.Product)
            .ToListAsync();

        var invoice = _libraryInvoicePdfService.GenerateLibraryInvoice(purchase, purchaseItems);
        await _emailService.SendTransactionSuccessEmailAsync(user.UserEmail, transaction, invoice);
    }

    private async Task<LibraryPackagePurchase?> FindActivePurchaseAsync(User user, LibraryPackage libraryPackage, DateTime now)
    {
        var previous = await _db.LibraryPackagePurchases
            .Where(p => p.UserId == user.UserId)
            .Include(p => p.LibraryPackage)
            .OrderByDescending(p => p.PurchaseDate)
            .ToListAsync();

        if (previous.Count == 0)
        {
            return null;
        }

        var latest = previous[0];
        var expiry = latest.PurchaseDate.AddDays(latest.LibraryPackage.ValidityDays);
        var stillValid = expiry > now;
        var samePackage = latest.PackageId == libraryPackage.PackageId;

        return stillValid && samePackage ? latest : null;
    }

    private async Task RecordRoyaltyAsync(Product product, TransactionItem item, decimal baseAmount, decimal royaltyPercent, decimal royaltyAmount, DateOnly today)
    {
        if (royaltyPercent <= 0)
        {
            return;
        }

        var beneficiaries = await _beneficiaryAssignmentService.GetAssignedBeneficiariesAsync(product);
        if (beneficiaries.Count == 0)
        {
            return;
        }

        var royaltyCalc = new RoyaltyCalculation
        {
            ProductId = product.ProductId,
            ItemId = item.ItemId,
            RoycalTranDate = today,
            TotalAmount = baseAmount,
            RoyaltyPercent = royaltyPercent,
            TotalRoyalty = royaltyAmount
        };
        _db.RoyaltyCalculations.Add(royaltyCalc);
        await _db.SaveChangesAsync();

        var perBeneficiary = Math.Round(royaltyAmount / beneficiaries.Count, MoneyScale, MidpointRounding.AwayFromZero);

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
