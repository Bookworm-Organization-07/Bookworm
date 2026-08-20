using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IBeneficiaryAssignmentService
{
    Task<List<Beneficiary>> GetAssignedBeneficiariesAsync(Product product);
    Task AssignAsync(Product product, int beneficiaryId);
    Task UnassignAsync(Product product, int beneficiaryId);
    Task<List<Beneficiary>> GetAssignmentsAsync(int productId);
}
