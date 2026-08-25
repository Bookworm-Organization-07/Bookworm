using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ICheckoutService
{
    Task<List<Transaction>> CheckoutAsync(User user);
}
