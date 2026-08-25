namespace BookwormApi.Models;

public class Cart
{
    public int CartId { get; set; }
    public int UserId { get; set; }
    public User? User { get; set; }
    public int ProductId { get; set; }
    public Product Product { get; set; } = null!;
    public int Qty { get; set; }
    public int? RentDays { get; set; }
}
