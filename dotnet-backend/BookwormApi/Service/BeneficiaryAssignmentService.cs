using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class BeneficiaryAssignmentService : IBeneficiaryAssignmentService
{
    private const string AuthorType = "Author";

    private readonly BookwormDbContext _db;

    public BeneficiaryAssignmentService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task<List<Beneficiary>> GetAssignedBeneficiariesAsync(Product product)
    {
        var assignments = await _db.BeneficiaryAssignments
            .Where(a => a.ProductId == product.ProductId)
            .Include(a => a.Beneficiary)
            .ToListAsync();

        if (assignments.Count > 0)
        {
            return assignments.Select(a => a.Beneficiary!).ToList();
        }

        Author? author = product.Author;
        if (author is null && product.AuthorId.HasValue)
        {
            author = await _db.Authors.FindAsync(product.AuthorId.Value);
        }

        if (author is null)
        {
            return new List<Beneficiary>();
        }

        var authorBeneficiary = await _db.Beneficiaries
            .FirstOrDefaultAsync(b => b.BeneficiaryType == AuthorType && b.BeneficiaryName.ToLower() == author.Name.ToLower());

        if (authorBeneficiary is null)
        {
            authorBeneficiary = new Beneficiary
            {
                BeneficiaryName = author.Name,
                BeneficiaryType = AuthorType
            };
            _db.Beneficiaries.Add(authorBeneficiary);
            await _db.SaveChangesAsync();
        }

        await AssignAsync(product, authorBeneficiary.BeneficiaryId);

        return new List<Beneficiary> { authorBeneficiary };
    }

    public async Task AssignAsync(Product product, int beneficiaryId)
    {
        var exists = await _db.BeneficiaryAssignments
            .AnyAsync(a => a.ProductId == product.ProductId && a.BeneficiaryId == beneficiaryId);
        if (exists)
        {
            return;
        }

        var beneficiary = await _db.Beneficiaries.FindAsync(beneficiaryId)
                           ?? throw new ArgumentException($"Beneficiary not found: {beneficiaryId}");

        _db.BeneficiaryAssignments.Add(new BeneficiaryAssignment
        {
            ProductId = product.ProductId,
            BeneficiaryId = beneficiary.BeneficiaryId
        });
        await _db.SaveChangesAsync();
    }

    public async Task UnassignAsync(Product product, int beneficiaryId)
    {
        var current = await _db.BeneficiaryAssignments
            .Where(a => a.ProductId == product.ProductId)
            .ToListAsync();

        var match = current.FirstOrDefault(a => a.BeneficiaryId == beneficiaryId);
        if (match is null)
        {
            throw new ArgumentException("That beneficiary is not assigned to this book.");
        }

        if (current.Count <= 1)
        {
            throw new InvalidOperationException("Can't remove the last beneficiary - every book needs at least one.");
        }

        _db.BeneficiaryAssignments.Remove(match);
        await _db.SaveChangesAsync();
    }

    public async Task<List<Beneficiary>> GetAssignmentsAsync(int productId)
    {
        return await _db.BeneficiaryAssignments
            .Where(a => a.ProductId == productId)
            .Include(a => a.Beneficiary)
            .Select(a => a.Beneficiary!)
            .ToListAsync();
    }
}
