using BookwormApi.DTO;

namespace BookwormApi.Service;

public interface IAdminDashboardService
{
    Task<AdminDashboardDto> GetDashboardAsync();
}
