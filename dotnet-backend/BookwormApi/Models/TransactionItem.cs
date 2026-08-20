namespace BookwormApi.Models;

public class TransactionItem
{
    public int ItemId { get; set; }
    public long? TransactionId { get; set; }
    public Transaction? Transaction { get; set; }
    public int? ProductId { get; set; }
    public Product? Product { get; set; }
    public decimal? Price { get; set; }
    public int? Quantity { get; set; }
}
