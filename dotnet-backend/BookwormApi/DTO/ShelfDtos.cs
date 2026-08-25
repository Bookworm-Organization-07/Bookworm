namespace BookwormApi.DTO;

public class MyShelfDto
{
    public int ShelfId { get; set; }
    public ProductDto Product { get; set; } = null!;
}
