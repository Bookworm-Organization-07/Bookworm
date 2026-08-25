namespace BookwormApi.Models;

public class LibraryPackagePurchaseItem
{
    public int ItemId { get; set; }
    public int PurchaseId { get; set; }
    public LibraryPackagePurchase? Purchase { get; set; }
    public int ProductId { get; set; }
    public Product Product { get; set; } = null!;
    public decimal RoyaltyPercent { get; set; }
    public decimal RoyaltyAmount { get; set; }
}
