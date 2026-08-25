using AutoMapper;
using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class ShelfService : IShelfService
{
    private readonly BookwormDbContext _db;
    private readonly IMapper _mapper;

    public ShelfService(BookwormDbContext db, IMapper mapper)
    {
        _db = db;
        _mapper = mapper;
    }

    public async Task<List<MyShelfDto>> GetShelfAsync(User user)
    {
        var shelf = await _db.MyShelves
            .Where(s => s.UserId == user.UserId)
            .Include(s => s.Product).ThenInclude(p => p!.Genere)
            .Include(s => s.Product).ThenInclude(p => p!.Language)
            .Include(s => s.Product).ThenInclude(p => p!.Author)
            .Include(s => s.Product).ThenInclude(p => p!.Publisher)
            .Include(s => s.Product).ThenInclude(p => p!.ProductType)
            .ToListAsync();
        return _mapper.Map<List<MyShelfDto>>(shelf);
    }

    public async Task RemoveAsync(User user, int shelfId)
    {
        var shelf = await _db.MyShelves.FirstOrDefaultAsync(s => s.ShelfId == shelfId && s.UserId == user.UserId)
                   ?? throw new ArgumentException("Shelf item not found.");
        _db.MyShelves.Remove(shelf);
        await _db.SaveChangesAsync();
    }

    public async Task AddToShelfAsync(User user, Product product)
    {
        var exists = await _db.MyShelves.AnyAsync(s => s.UserId == user.UserId && s.ProductId == product.ProductId);
        if (exists)
        {
            return;
        }
        _db.MyShelves.Add(new MyShelf { UserId = user.UserId, ProductId = product.ProductId });
        await _db.SaveChangesAsync();
    }
}
