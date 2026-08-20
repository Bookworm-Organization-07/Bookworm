using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class ProductCoverService : IProductCoverService
{
    private readonly BookwormDbContext _db;

    public ProductCoverService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task SaveCoverAsync(int productId, Stream fileStream, string contentType)
    {
        var product = await _db.Products.FirstOrDefaultAsync(p => p.ProductId == productId)
                     ?? throw new ArgumentException($"Product not found: {productId}");

        using var ms = new MemoryStream();
        await fileStream.CopyToAsync(ms);
        var bytes = ms.ToArray();

        var cover = await _db.ProductCovers.FirstOrDefaultAsync(c => c.ProductId == productId);
        if (cover is null)
        {
            cover = new ProductCover { ProductId = productId };
            _db.ProductCovers.Add(cover);
        }
        cover.ImageData = bytes;
        cover.ContentType = contentType;

        product.ProductImage = $"/api/products/{productId}/cover";

        await _db.SaveChangesAsync();
    }

    public async Task<(byte[] Data, string ContentType)> GetCoverAsync(int productId)
    {
        var cover = await _db.ProductCovers.FirstOrDefaultAsync(c => c.ProductId == productId)
                   ?? throw new ArgumentException("No cover image for this product.");
        return (cover.ImageData, cover.ContentType);
    }
}
