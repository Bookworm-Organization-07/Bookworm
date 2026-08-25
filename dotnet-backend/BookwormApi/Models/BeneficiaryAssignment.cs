namespace BookwormApi.Models;

public class BeneficiaryAssignment
{
    public int AssignmentId { get; set; }
    public int ProductId { get; set; }
    public Product? Product { get; set; }
    public int BeneficiaryId { get; set; }
    public Beneficiary? Beneficiary { get; set; }
}
