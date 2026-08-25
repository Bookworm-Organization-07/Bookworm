using BookwormApi.Data;
using BookwormApi.DTO;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class ProductBeneficiaryService : IProductBeneficiaryService
{
    private readonly BookwormDbContext _db;

    public ProductBeneficiaryService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task<List<ProductBeneficiaryDTO>> GetAllAsync()
    {
        // Starts from Beneficiaries (not ProductBeneficiaries) and left-joins
        // in whatever royalty rows they've earned, so a beneficiary with no
        // product assigned yet - or assigned but nothing earned so far - is
        // still in the list, just with a null product and no amount. Starting
        // from ProductBeneficiaries instead (the earlier version) meant a
        // beneficiary only ever showed up once their first royalty payout had
        // actually happened - so with none yet, the whole page was empty.
        var rows = await _db.Beneficiaries
            .GroupJoin(
                _db.ProductBeneficiaries.Include(pb => pb.Product),
                beneficiary => beneficiary.BeneficiaryId,
                pb => pb.BeneficiaryId,
                (beneficiary, earned) => new { beneficiary, earned })
            .SelectMany(
                x => x.earned.DefaultIfEmpty(),
                (x, pb) => new ProductBeneficiaryDTO
                {
                    ProdbenId = pb != null ? pb.ProdbenId : 0,
                    BeneficiaryName = x.beneficiary.BeneficiaryName,
                    ProductName = pb != null ? pb.Product!.ProductName : null,
                    RoyaltyReceived = pb != null ? pb.RoyaltyReceived : null
                })
            .OrderBy(dto => dto.BeneficiaryName)
            .ToListAsync();

        return rows;
    }
}
