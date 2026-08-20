using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class ReadBookService : IReadBookService
{
    private readonly BookwormDbContext _db;

    public ReadBookService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task SavePdfAsync(int productId, Stream fileStream, string fileName)
    {
        var product = await _db.Products.FirstOrDefaultAsync(p => p.ProductId == productId)
                     ?? throw new ArgumentException($"Product not found: {productId}");

        using var ms = new MemoryStream();
        await fileStream.CopyToAsync(ms);
        var bytes = ms.ToArray();

        var readBook = await _db.ReadBooks.FirstOrDefaultAsync(r => r.ProductId == productId);
        if (readBook is null)
        {
            readBook = new ReadBook { ProductId = productId };
            _db.ReadBooks.Add(readBook);
        }
        readBook.PdfData = bytes;
        readBook.FileName = fileName;

        await _db.SaveChangesAsync();
    }

    public async Task<(byte[] Data, string FileName)> ReadBookAsync(int productId)
    {
        var readBook = await _db.ReadBooks.FirstOrDefaultAsync(r => r.ProductId == productId)
                       ?? throw new ArgumentException("No PDF has been uploaded for this book.");
        return (readBook.PdfData ?? Array.Empty<byte>(), readBook.FileName ?? "book.pdf");
    }
}
