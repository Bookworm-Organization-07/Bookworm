using System.ComponentModel.DataAnnotations;

namespace BookwormApi.DTO;

public class CartDto
{
    public int CartId { get; set; }
    public int Qty { get; set; }
    public int? RentDays { get; set; }
    public ProductDto Product { get; set; } = null!;
}

public class AddToCartRequest
{
    [Required]
    public int? ProductId { get; set; }
    public int? Qty { get; set; }
    public int? RentDays { get; set; }
}

public class UpdateCartQtyRequest
{
    public int? Qty { get; set; }
}
