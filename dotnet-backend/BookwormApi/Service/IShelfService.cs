using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IShelfService
{
    Task<List<MyShelfDto>> GetShelfAsync(User user);
    Task RemoveAsync(User user, int shelfId);
    Task AddToShelfAsync(User user, Product product);
}
