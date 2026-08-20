namespace BookwormApi.Models;

public class ProductBeneficiary
{
    public int ProdbenId { get; set; }
    public int BeneficiaryId { get; set; }
    public Beneficiary? Beneficiary { get; set; }
    public int ProductId { get; set; }
    public Product? Product { get; set; }
    public int RoycalId { get; set; }
    public RoyaltyCalculation? RoyaltyCalculation { get; set; }
    public decimal? RoyaltyReceived { get; set; }
}
