namespace BookwormApi.DTO;

public class OrderHistoryDTO
{
    public long TransactionId { get; set; }
    public string? ProductName { get; set; }
    public decimal? Amount { get; set; }
    public DateTime? OrderDate { get; set; }
}
