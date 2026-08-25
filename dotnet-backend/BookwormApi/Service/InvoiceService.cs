using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class InvoiceService : IInvoiceService
{
    private readonly BookwormDbContext _db;
    private readonly ITransactionPdfService _transactionPdfService;
    private readonly ILibraryInvoicePdfService _libraryInvoicePdfService;

    public InvoiceService(
        BookwormDbContext db,
        ITransactionPdfService transactionPdfService,
        ILibraryInvoicePdfService libraryInvoicePdfService)
    {
        _db = db;
        _transactionPdfService = transactionPdfService;
        _libraryInvoicePdfService = libraryInvoicePdfService;
    }

    public async Task<byte[]> GetInvoicePdfAsync(User user, long transactionId)
    {
        var transaction = await _db.Transactions
            .Include(t => t.User)
            .FirstOrDefaultAsync(t => t.TransactionId == transactionId)
            ?? throw new ArgumentException("Transaction not found.");

        if (transaction.UserId != user.UserId)
        {
            throw new ArgumentException("Transaction not found.");
        }

        if (transaction.Status != TransactionStatus.SUCCESS)
        {
            throw new ArgumentException("Invoice is not available for this transaction.");
        }

        if (transaction.TransactionType == TransactionType.LEND)
        {
            var purchase = await _db.LibraryPackagePurchases
                .Include(p => p.User)
                .Include(p => p.LibraryPackage)
                .FirstOrDefaultAsync(p => p.TransactionId == transactionId)
                ?? throw new ArgumentException("Invoice is not available for this transaction.");

            var items = await _db.LibraryPackagePurchaseItems
                .Where(i => i.PurchaseId == purchase.PurchaseId)
                .Include(i => i.Product)
                .ToListAsync();

            return _libraryInvoicePdfService.GenerateLibraryInvoice(purchase, items);
        }

        var txItems = await _db.TransactionItems
            .Where(i => i.TransactionId == transactionId)
            .Include(i => i.Product)
            .ToListAsync();

        return _transactionPdfService.GenerateInvoice(transaction, txItems);
    }
}
