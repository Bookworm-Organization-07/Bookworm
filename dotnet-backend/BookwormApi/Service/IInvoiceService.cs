using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IInvoiceService
{
    Task<byte[]> GetInvoicePdfAsync(User user, long transactionId);
}
