using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IMyLibraryService
{
    Task<List<MyLibraryItemResponseDto>> GetUserLibraryAsync(User user);
    Task<(byte[] Data, string FileName)> ReadLibraryBookAsync(User user, int productId);
    Task RecordRentalAsync(User user, Product product, int rentDays);
}
