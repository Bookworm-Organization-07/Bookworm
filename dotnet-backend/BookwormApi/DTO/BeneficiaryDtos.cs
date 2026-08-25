namespace BookwormApi.DTO;

public class BeneficiaryDto
{
    public int BeneficiaryId { get; set; }
    public string BeneficiaryName { get; set; } = null!;
    public string BeneficiaryType { get; set; } = "Other";
    public string? BeneficiaryEmailId { get; set; }
    public string? BeneficiaryContactNo { get; set; }
    public string? BeneficiaryBankName { get; set; }
    public string? BeneficiaryBankBranch { get; set; }
    public string? BeneficiaryIfsc { get; set; }
    public string? BeneficiaryAccNo { get; set; }
    public string? BeneficiaryAccType { get; set; }
    public string? BeneficiaryPan { get; set; }
}

public class ProductBeneficiaryDTO
{
    public int ProdbenId { get; set; }
    public string? BeneficiaryName { get; set; }
    public string? ProductName { get; set; }
    public decimal? RoyaltyReceived { get; set; }
}
