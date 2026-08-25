using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class MyLibraryService : IMyLibraryService
{
    private readonly BookwormDbContext _db;
    private readonly IReadBookService _readBookService;

    public MyLibraryService(BookwormDbContext db, IReadBookService readBookService)
    {
        _db = db;
        _readBookService = readBookService;
    }

    public async Task<List<MyLibraryItemResponseDto>> GetUserLibraryAsync(User user)
    {
        var today = DateOnly.FromDateTime(DateTime.Today);
        var rows = await _db.MyLibraries
            .Where(m => m.UserId == user.UserId && m.EndDate != null && m.EndDate > today)
            .Include(m => m.Product).ThenInclude(p => p!.Author)
            .Include(m => m.LibraryPackage)
            .ToListAsync();

        return rows
            .Where(m => m.Product is not null)
            .Select(m => new MyLibraryItemResponseDto
            {
                MyLibId = m.MyLibId,
                AccessType = m.AccessType.ToString(),
                PackageId = m.PackageId,
                PackageName = m.LibraryPackage?.Name,
                StartDate = m.StartDate,
                EndDate = m.EndDate,
                Product = new ProductResponseDto
                {
                    ProductId = m.Product!.ProductId,
                    ProductName = m.Product.ProductName,
                    ProductImage = m.Product.ProductImage,
                    AuthorName = m.Product.Author?.Name
                }
            })
            .ToList();
    }

    public async Task<(byte[] Data, string FileName)> ReadLibraryBookAsync(User user, int productId)
    {
        var today = DateOnly.FromDateTime(DateTime.Today);
        var allowed = await _db.MyLibraries.AnyAsync(m =>
            m.UserId == user.UserId && m.ProductId == productId && m.EndDate != null && m.EndDate > today);

        if (!allowed)
        {
            throw new ArgumentException("This title is not in your library.");
        }

        return await _readBookService.ReadBookAsync(productId);
    }

    public async Task RecordRentalAsync(User user, Product product, int rentDays)
    {
        var today = DateOnly.FromDateTime(DateTime.Today);
        var newEndDate = today.AddDays(rentDays);

        var existing = await _db.MyLibraries.FirstOrDefaultAsync(m =>
            m.UserId == user.UserId && m.ProductId == product.ProductId &&
            m.AccessType == LibraryAccessType.RENT && m.EndDate != null && m.EndDate > today);

        if (existing is not null)
        {
            if (newEndDate > existing.EndDate!.Value)
            {
                existing.EndDate = newEndDate;
                await _db.SaveChangesAsync();
            }
            return;
        }

        _db.MyLibraries.Add(new MyLibrary
        {
            UserId = user.UserId,
            ProductId = product.ProductId,
            AccessType = LibraryAccessType.RENT,
            StartDate = today,
            EndDate = newEndDate
        });
        await _db.SaveChangesAsync();
    }
}
