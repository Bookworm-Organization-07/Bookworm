using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ILibraryCheckoutService
{
    Task CheckoutAsync(User user, LibraryCheckoutRequest request);
}
