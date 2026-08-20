namespace BookwormApi.Models;

public class ProductAttribute
{
    public int ProdAttId { get; set; }
    public int ProductId { get; set; }
    public Product? Product { get; set; }
    public int AttributeId { get; set; }
    public AttributeEntity? Attribute { get; set; }
    public string? AttributeValue { get; set; }
}
