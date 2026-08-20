using BookwormApi.Data;
using BookwormApi.DTO;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class RoyaltyLedgerService : IRoyaltyLedgerService
{
    private readonly BookwormDbContext _db;

    public RoyaltyLedgerService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task<List<RoyaltyLedgerEntryDto>> GetLedgerAsync()
    {
        var rows = await _db.RoyaltyCalculations
            .Include(r => r.Product)
            .Include(r => r.TransactionItem)
            .ThenInclude(ti => ti!.Transaction)
            .OrderByDescending(r => r.RoycalTranDate)
            .ThenByDescending(r => r.RoycalId)
            .ToListAsync();

        return rows.Select(r => new RoyaltyLedgerEntryDto
        {
            RoycalId = r.RoycalId,
            TranDate = r.RoycalTranDate,
            ProductName = r.Product?.ProductName,
            TransactionId = r.TransactionItem?.Transaction?.TransactionId ?? 0,
            TransactionType = r.TransactionItem?.Transaction?.TransactionType?.ToString(),
            TotalAmount = r.TotalAmount,
            RoyaltyPercent = r.RoyaltyPercent,
            TotalRoyalty = r.TotalRoyalty
        }).ToList();
    }
}
