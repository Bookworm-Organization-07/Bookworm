namespace BookwormApi.Models;

public class MyShelf
{
    public int ShelfId { get; set; }
    public int UserId { get; set; }
    public User? User { get; set; }
    public int ProductId { get; set; }
    public Product Product { get; set; } = null!;
}
