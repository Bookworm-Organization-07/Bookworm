namespace BookwormApi.Models;

public class MyLibrary
{
    public int MyLibId { get; set; }
    public int UserId { get; set; }
    public User? User { get; set; }
    public int? PackageId { get; set; }
    public LibraryPackage? LibraryPackage { get; set; }
    public int? ProductId { get; set; }
    public Product? Product { get; set; }
    public LibraryAccessType AccessType { get; set; }
    public DateOnly? StartDate { get; set; }
    public DateOnly? EndDate { get; set; }
    public int? BooksAllowed { get; set; }
    public int? BooksTaken { get; set; }
}
