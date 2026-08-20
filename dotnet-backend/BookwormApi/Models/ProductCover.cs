namespace BookwormApi.Models;

public class ProductCover
{
    public int CoverId { get; set; }
    public byte[] ImageData { get; set; } = null!;
    public string ContentType { get; set; } = null!;
    public int ProductId { get; set; }
    public Product? Product { get; set; }
}
