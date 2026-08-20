using AutoMapper;
using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class TransactionService : ITransactionService
{
    private readonly BookwormDbContext _db;
    private readonly IMapper _mapper;

    public TransactionService(BookwormDbContext db, IMapper mapper)
    {
        _db = db;
        _mapper = mapper;
    }

    public async Task<List<TransactionDto>> GetMineAsync(User user)
    {
        var transactions = await _db.Transactions
            .Where(t => t.UserId == user.UserId)
            .OrderByDescending(t => t.CreatedAt)
            .ToListAsync();
        return _mapper.Map<List<TransactionDto>>(transactions);
    }

    public async Task<List<TransactionItemDTO>> GetItemsAsync(User user, long transactionId)
    {
        var transaction = await _db.Transactions.FirstOrDefaultAsync(t => t.TransactionId == transactionId)
                          ?? throw new ArgumentException("Transaction not found.");
        if (transaction.UserId != user.UserId)
        {
            throw new ArgumentException("Transaction not found.");
        }

        var items = await _db.TransactionItems
            .Where(i => i.TransactionId == transactionId)
            .Include(i => i.Product)
            .ToListAsync();

        return _mapper.Map<List<TransactionItemDTO>>(items);
    }
}
