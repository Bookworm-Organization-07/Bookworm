using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ITransactionPdfService
{
    byte[] GenerateInvoice(Transaction transaction, List<TransactionItem> items);
}
