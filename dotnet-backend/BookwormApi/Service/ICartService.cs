using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ICartService
{
    Task<List<CartDto>> GetCartAsync(User user);
    Task<CartDto> AddToCartAsync(User user, int productId, int? qty, int? rentDays);
    Task<CartDto> UpdateQtyAsync(User user, int cartId, int? qty);
    Task RemoveItemAsync(User user, int cartId);
    Task ClearCartAsync(User user);
}
