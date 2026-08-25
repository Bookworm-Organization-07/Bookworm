namespace BookwormApi.Models;

public class LibraryPackage
{
    public int PackageId { get; set; }
    public string Name { get; set; } = null!;
    public decimal Cost { get; set; }
    public int ValidityDays { get; set; }
    public int BookLimit { get; set; }
    public string Description { get; set; } = null!;
}
