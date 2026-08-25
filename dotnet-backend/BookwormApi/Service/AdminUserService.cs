using AutoMapper;
using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class AdminUserService : IAdminUserService
{
    private readonly BookwormDbContext _db;
    private readonly IMapper _mapper;

    public AdminUserService(BookwormDbContext db, IMapper mapper)
    {
        _db = db;
        _mapper = mapper;
    }

    public async Task<List<AdminUserSummaryDto>> GetAllUsersAsync()
    {
        var users = await _db.Users.ToListAsync();
        return _mapper.Map<List<AdminUserSummaryDto>>(users);
    }

    public async Task DeleteUserAsync(int userId, User callingAdmin)
    {
        if (userId == callingAdmin.UserId)
        {
            throw new ArgumentException("You cannot delete your own account.");
        }

        var user = await _db.Users.FirstOrDefaultAsync(u => u.UserId == userId)
                  ?? throw new ArgumentException($"User not found: {userId}");

        await using var tx = await _db.Database.BeginTransactionAsync();

        var carts = await _db.Carts.Where(c => c.UserId == userId).ToListAsync();
        _db.Carts.RemoveRange(carts);

        var shelf = await _db.MyShelves.Where(s => s.UserId == userId).ToListAsync();
        _db.MyShelves.RemoveRange(shelf);

        var library = await _db.MyLibraries.Where(m => m.UserId == userId).ToListAsync();
        _db.MyLibraries.RemoveRange(library);

        var purchases = await _db.LibraryPackagePurchases
            .Where(p => p.UserId == userId)
            .OrderByDescending(p => p.PurchaseDate)
            .ToListAsync();
        foreach (var purchase in purchases)
        {
            var items = await _db.LibraryPackagePurchaseItems.Where(i => i.PurchaseId == purchase.PurchaseId).ToListAsync();
            _db.LibraryPackagePurchaseItems.RemoveRange(items);
        }
        _db.LibraryPackagePurchases.RemoveRange(purchases);
        await _db.SaveChangesAsync();

        var transactions = await _db.Transactions
            .Where(t => t.UserId == userId)
            .OrderByDescending(t => t.CreatedAt)
            .ToListAsync();
        foreach (var t in transactions)
        {
            t.UserId = null;
        }
        await _db.SaveChangesAsync();

        _db.Users.Remove(user);
        await _db.SaveChangesAsync();

        await tx.CommitAsync();
    }
}
