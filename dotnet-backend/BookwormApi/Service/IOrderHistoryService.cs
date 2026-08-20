using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IOrderHistoryService
{
    Task<List<OrderHistoryDTO>> GetMineAsync(User user);
    Task<List<OrderHistoryDTO>> GetAllAsync();
}
