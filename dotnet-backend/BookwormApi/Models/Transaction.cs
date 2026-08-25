namespace BookwormApi.Models;

public class Transaction
{
    public long TransactionId { get; set; }
    public int? UserId { get; set; }
    public User? User { get; set; }
    public decimal? TotalAmount { get; set; }
    public TransactionStatus? Status { get; set; }
    public DateTime? CreatedAt { get; set; }
    public TransactionType? TransactionType { get; set; }

    public List<TransactionItem> Items { get; set; } = new();
}
