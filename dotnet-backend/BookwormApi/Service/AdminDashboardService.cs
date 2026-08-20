using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class AdminDashboardService : IAdminDashboardService
{
    private readonly BookwormDbContext _db;

    public AdminDashboardService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task<AdminDashboardDto> GetDashboardAsync()
    {
        var totalRevenue = await _db.Transactions
            .Where(t => t.Status == TransactionStatus.SUCCESS)
            .SumAsync(t => t.TotalAmount ?? 0m);

        // Not just BUY - a purchase, a rental, or a library lend all count as a book reaching a
        // reader (matches Java's AdminDashboardService, which totals every successful
        // transaction's items with no type filter).
        var soldItems = _db.TransactionItems
            .Include(i => i.Transaction)
            .Where(i => i.Transaction != null &&
                        i.Transaction.Status == TransactionStatus.SUCCESS);

        var booksBought = await soldItems.SumAsync(i => (long)(i.Quantity ?? 0));

        var bestseller = await soldItems
            .Include(i => i.Product)
            .GroupBy(i => new { i.ProductId, ProductName = i.Product != null ? i.Product.ProductName : null })
            .Select(g => new { g.Key.ProductName, Count = g.Sum(x => (long)(x.Quantity ?? 0)) })
            .OrderByDescending(g => g.Count)
            .FirstOrDefaultAsync();

        return new AdminDashboardDto
        {
            TotalRevenue = totalRevenue,
            BooksBought = booksBought,
            BestsellerProductName = bestseller?.ProductName,
            BestsellerCount = bestseller?.Count ?? 0
        };
    }
}
