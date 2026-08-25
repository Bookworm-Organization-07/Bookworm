using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ITransactionService
{
    Task<List<TransactionDto>> GetMineAsync(User user);
    Task<List<TransactionItemDTO>> GetItemsAsync(User user, long transactionId);
}
