using BookwormApi.DTO;

namespace BookwormApi.Service;

public interface IRoyaltyLedgerService
{
    Task<List<RoyaltyLedgerEntryDto>> GetLedgerAsync();
}
