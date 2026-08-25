using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IProductService
{
    Task<List<ProductDto>> GetAllAsync();
    Task<ProductDto> GetByIdAsync(int id);
    Task<Product> GetEntityByIdAsync(int id);
    Task<List<ProductDto>> SearchAsync(string? name, int? limit);
    Task<List<ProductDto>> GetLibraryProductsAsync(string? genere, string? language);
    Task<List<ProductDto>> FilterProductsAsync(string? genere, string? language);
    Task<ProductDto> CreateAsync(ProductRequestDto request);
    Task<ProductDto> UpdateAsync(int id, ProductRequestDto request);
    Task DeleteAsync(int id);
    Task<ProductDto> UpdateRoyaltyPercentAsync(int id, decimal? royaltyPercent);
}
