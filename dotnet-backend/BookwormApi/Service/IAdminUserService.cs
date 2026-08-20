using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IAdminUserService
{
    Task<List<AdminUserSummaryDto>> GetAllUsersAsync();
    Task DeleteUserAsync(int userId, User callingAdmin);
}
