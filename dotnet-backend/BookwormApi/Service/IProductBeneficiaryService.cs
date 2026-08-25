using BookwormApi.DTO;

namespace BookwormApi.Service;

public interface IProductBeneficiaryService
{
    Task<List<ProductBeneficiaryDTO>> GetAllAsync();
}
