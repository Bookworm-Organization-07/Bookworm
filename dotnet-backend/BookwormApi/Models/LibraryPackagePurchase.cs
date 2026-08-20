namespace BookwormApi.Models;

public class LibraryPackagePurchase
{
    public int PurchaseId { get; set; }
    public long TransactionId { get; set; }
    public Transaction? Transaction { get; set; }
    public int UserId { get; set; }
    public User? User { get; set; }
    public int PackageId { get; set; }
    public LibraryPackage LibraryPackage { get; set; } = null!;
    public decimal PackagePrice { get; set; }
    public int AllowedBooks { get; set; }
    public decimal AvgBookPrice { get; set; }
    public DateTime PurchaseDate { get; set; }

    public List<LibraryPackagePurchaseItem> Items { get; set; } = new();
}
