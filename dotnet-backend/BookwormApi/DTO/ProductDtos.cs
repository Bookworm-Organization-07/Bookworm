namespace BookwormApi.DTO;

public class GenereDto
{
    public int GenereId { get; set; }
    public string GenereDesc { get; set; } = null!;
}

public class LanguageDto
{
    public int LanguageId { get; set; }
    public string LanguageDesc { get; set; } = null!;
}

public class AuthorDto
{
    public int AuthorId { get; set; }
    public string Name { get; set; } = null!;
}

public class PublisherDto
{
    public int PublisherId { get; set; }
    public string Name { get; set; } = null!;
}

public class ProductTypeDto
{
    public int TypeId { get; set; }
    public string TypeDesc { get; set; } = null!;
}

public class ProductDto
{
    public int ProductId { get; set; }
    public string ProductName { get; set; } = null!;
    public string? ProductNameEnglish { get; set; }
    public string? ProductImage { get; set; }
    public string? ProductDescriptionShort { get; set; }
    public string? ProductDescriptionLong { get; set; }
    public decimal ProductBaseprice { get; set; }
    public decimal? ProductOfferprice { get; set; }
    public DateOnly? ProductOffPriceExpirydate { get; set; }
    public decimal? DiscountPercent { get; set; }
    public string ProductIsbn { get; set; } = null!;
    public bool Rentable { get; set; }
    public decimal? RentPerDay { get; set; }
    public int MinRentDays { get; set; }
    public bool Library { get; set; }
    public decimal? RoyaltyPercent { get; set; }
    public GenereDto? Genere { get; set; }
    public LanguageDto? Language { get; set; }
    public AuthorDto? Author { get; set; }
    public PublisherDto? Publisher { get; set; }
    public ProductTypeDto? ProductType { get; set; }
}

public class ProductResponseDto
{
    public int ProductId { get; set; }
    public string ProductName { get; set; } = null!;
    public string? ProductImage { get; set; }
    public string? AuthorName { get; set; }
}

public class ProductRequestDto
{
    public string ProductName { get; set; } = null!;
    public string? ProductNameEnglish { get; set; }
    public int? ProductTypeId { get; set; }
    public int? AuthorId { get; set; }
    public int? PublisherId { get; set; }
    public int? LanguageId { get; set; }
    public int? GenereId { get; set; }
    public decimal ProductBaseprice { get; set; }
    public decimal? ProductOfferprice { get; set; }
    public decimal? DiscountPercent { get; set; }
    public decimal? RoyaltyPercent { get; set; }
    public DateOnly? ProductOffPriceExpirydate { get; set; }
    public string? ProductDescriptionShort { get; set; }
    public string? ProductDescriptionLong { get; set; }
    public string? ProductIsbn { get; set; }
    public bool Rentable { get; set; }
    public bool Library { get; set; }
    public decimal? RentPerDay { get; set; }
    public int MinRentDays { get; set; }
    public string? ProductImage { get; set; }
}

public class UpdateRoyaltyRequest
{
    public decimal? RoyaltyPercent { get; set; }
}
