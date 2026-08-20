namespace BookwormApi.DTO;

public class LibraryPackageDto
{
    public int PackageId { get; set; }
    public string Name { get; set; } = null!;
    public decimal Cost { get; set; }
    public int ValidityDays { get; set; }
    public int BookLimit { get; set; }
    public string Description { get; set; } = null!;
}

public class LibraryCheckoutRequest
{
    public int? PackageId { get; set; }
    public List<int>? ProductIds { get; set; }
}

public class MyLibraryItemResponseDto
{
    public int MyLibId { get; set; }
    public string AccessType { get; set; } = null!;
    public int? PackageId { get; set; }
    public string? PackageName { get; set; }
    public DateOnly? StartDate { get; set; }
    public DateOnly? EndDate { get; set; }
    public ProductResponseDto Product { get; set; } = null!;
}
