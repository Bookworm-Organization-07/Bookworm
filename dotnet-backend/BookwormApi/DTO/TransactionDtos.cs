namespace BookwormApi.DTO;

public class TransactionDto
{
    public long TransactionId { get; set; }
    public int? UserId { get; set; }
    public decimal? TotalAmount { get; set; }
    public string? Status { get; set; }
    public DateTime? CreatedAt { get; set; }
    public string? TransactionType { get; set; }
}

public class TransactionItemDTO
{
    public int ItemId { get; set; }
    public long? TransactionId { get; set; }
    public int? ProductId { get; set; }
    public string? ProductName { get; set; }
    public int? Quantity { get; set; }
    public decimal? Price { get; set; }
}

public class CheckoutTransactionSummary
{
    public long TransactionId { get; set; }
    public decimal? TotalAmount { get; set; }
    public string? TransactionType { get; set; }
}

public class CheckoutResponseDto
{
    public List<CheckoutTransactionSummary> Transactions { get; set; } = new();
}
