using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class OrderHistoryService : IOrderHistoryService
{
    private readonly BookwormDbContext _db;

    public OrderHistoryService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task<List<OrderHistoryDTO>> GetMineAsync(User user)
    {
        return await Query(_db.TransactionItems.Where(i => i.Transaction != null && i.Transaction.UserId == user.UserId));
    }

    public async Task<List<OrderHistoryDTO>> GetAllAsync()
    {
        return await Query(_db.TransactionItems);
    }

    private static async Task<List<OrderHistoryDTO>> Query(IQueryable<TransactionItem> source)
    {
        var rows = await source
            .Include(i => i.Transaction)
            .Include(i => i.Product)
            .Where(i => i.Transaction != null && i.Transaction.Status == TransactionStatus.SUCCESS)
            .OrderByDescending(i => i.Transaction!.CreatedAt)
            .ToListAsync();

        return rows.Select(i => new OrderHistoryDTO
        {
            TransactionId = i.Transaction!.TransactionId,
            ProductName = i.Product?.ProductName,
            Amount = (i.Price ?? 0m) * (i.Quantity ?? 0),
            OrderDate = i.Transaction.CreatedAt
        }).ToList();
    }
}
