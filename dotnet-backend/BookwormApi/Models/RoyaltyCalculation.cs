namespace BookwormApi.Models;

public class RoyaltyCalculation
{
    public int RoycalId { get; set; }
    public int ItemId { get; set; }
    public TransactionItem? TransactionItem { get; set; }
    public DateOnly? RoycalTranDate { get; set; }
    public int ProductId { get; set; }
    public Product? Product { get; set; }
    public decimal? TotalAmount { get; set; }
    public decimal? RoyaltyPercent { get; set; }
    public decimal? TotalRoyalty { get; set; }
}
