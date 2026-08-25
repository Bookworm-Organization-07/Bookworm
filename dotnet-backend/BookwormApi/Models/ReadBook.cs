namespace BookwormApi.Models;

public class ReadBook
{
    public int PdfId { get; set; }
    public byte[]? PdfData { get; set; }
    public string? FileName { get; set; }
    public int ProductId { get; set; }
    public Product? Product { get; set; }
}
