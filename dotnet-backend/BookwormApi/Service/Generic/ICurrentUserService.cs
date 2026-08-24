using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ICurrentUserService
{
    Task<User> RequireAsync();
}
