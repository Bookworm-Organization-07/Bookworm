namespace BookwormApi.DTO;

public class RoyaltyLedgerEntryDto
{
    public int RoycalId { get; set; }
    public DateOnly? TranDate { get; set; }
    public string? ProductName { get; set; }
    public long TransactionId { get; set; }
    public string? TransactionType { get; set; }
    public decimal? TotalAmount { get; set; }
    public decimal? RoyaltyPercent { get; set; }
    public decimal? TotalRoyalty { get; set; }
}
