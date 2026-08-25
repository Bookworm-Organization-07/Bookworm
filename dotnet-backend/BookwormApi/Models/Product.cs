namespace BookwormApi.Models;

public class Product
{
    public int ProductId { get; set; }
    public string ProductName { get; set; } = null!;
    public string? ProductNameEnglish { get; set; }

    public int? ProductTypeId { get; set; }
    public ProductType? ProductType { get; set; }

    public int? AuthorId { get; set; }
    public Author? Author { get; set; }

    public int? PublisherId { get; set; }
    public Publisher? Publisher { get; set; }

    public int? LanguageId { get; set; }
    public Language? Language { get; set; }

    public int? GenereId { get; set; }
    public Genere? Genere { get; set; }

    public decimal ProductBaseprice { get; set; }
    public decimal? ProductOfferprice { get; set; }
    public decimal? DiscountPercent { get; set; }
    public decimal? RoyaltyPercent { get; set; }
    public DateOnly? ProductOffPriceExpirydate { get; set; }
    public string? ProductDescriptionShort { get; set; }
    public string? ProductDescriptionLong { get; set; }
    public string ProductIsbn { get; set; } = null!;
    public bool Rentable { get; set; }
    public bool Library { get; set; }
    public decimal? RentPerDay { get; set; }
    public int MinRentDays { get; set; }
    public string? ProductImage { get; set; }
}
