using AutoMapper;
using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class ProductService : IProductService
{
    private const int DefaultSearchLimit = 10;
    private const int MaxSearchLimit = 50;

    private readonly BookwormDbContext _db;
    private readonly IMapper _mapper;
    private readonly ILogger<ProductService> _logger;

    public ProductService(BookwormDbContext db, IMapper mapper, ILogger<ProductService> logger)
    {
        _db = db;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<List<ProductDto>> GetAllAsync()
    {
        var products = await _db.Products.IncludeProductDetails().ToListAsync();
        return _mapper.Map<List<ProductDto>>(products);
    }

    public async Task<ProductDto> GetByIdAsync(int id)
    {
        var product = await GetEntityByIdAsync(id);
        return _mapper.Map<ProductDto>(product);
    }

    public async Task<Product> GetEntityByIdAsync(int id)
    {
        return await _db.Products.IncludeProductDetails().FirstOrDefaultAsync(p => p.ProductId == id)
               ?? throw new ArgumentException($"Product not found: {id}");
    }

    public async Task<List<ProductDto>> SearchAsync(string? name, int? limit)
    {
        var query = (name ?? string.Empty).Trim();
        if (query.Length == 0)
        {
            return new List<ProductDto>();
        }

        var safeLimit = limit is null ? DefaultSearchLimit : Math.Clamp(limit.Value, 1, MaxSearchLimit);

        var lowered = query.ToLower();
        var products = await _db.Products
            .IncludeProductDetails()
            .Where(p => p.ProductName.ToLower().Contains(lowered) ||
                        (p.ProductNameEnglish != null && p.ProductNameEnglish.ToLower().Contains(lowered)))
            .Take(safeLimit)
            .ToListAsync();

        return _mapper.Map<List<ProductDto>>(products);
    }

    public async Task<List<ProductDto>> GetLibraryProductsAsync(string? genere, string? language)
    {
        var query = _db.Products.IncludeProductDetails().Where(p => p.Library);
        query = ApplyGenereLanguageFilter(query, genere, language);
        var products = await query.ToListAsync();
        return _mapper.Map<List<ProductDto>>(products);
    }

    public async Task<List<ProductDto>> FilterProductsAsync(string? genere, string? language)
    {
        var query = _db.Products.IncludeProductDetails();
        query = ApplyGenereLanguageFilter(query, genere, language);
        var products = await query.ToListAsync();
        return _mapper.Map<List<ProductDto>>(products);
    }

    private static IQueryable<Product> ApplyGenereLanguageFilter(IQueryable<Product> query, string? genere, string? language)
    {
        var genereSet = !string.IsNullOrWhiteSpace(genere);
        var languageSet = !string.IsNullOrWhiteSpace(language);

        if (genereSet)
        {
            query = query.Where(p => p.Genere != null && p.Genere.GenereDesc == genere);
        }
        if (languageSet)
        {
            query = query.Where(p => p.Language != null && p.Language.LanguageDesc == language);
        }
        return query;
    }

    public async Task<ProductDto> CreateAsync(ProductRequestDto request)
    {
        var product = _mapper.Map<Product>(request);
        product.ProductIsbn ??= $"BU-{Guid.NewGuid():N}"[..15].ToUpperInvariant();
        _db.Products.Add(product);
        await _db.SaveChangesAsync();
        var created = await GetEntityByIdAsync(product.ProductId);
        return _mapper.Map<ProductDto>(created);
    }

    public async Task<ProductDto> UpdateAsync(int id, ProductRequestDto request)
    {
        var existing = await _db.Products.FirstOrDefaultAsync(p => p.ProductId == id)
                       ?? throw new ArgumentException($"Product not found: {id}");

        if (request.ProductName is not null) existing.ProductName = request.ProductName;
        if (request.ProductDescriptionShort is not null) existing.ProductDescriptionShort = request.ProductDescriptionShort;
        if (request.ProductDescriptionLong is not null) existing.ProductDescriptionLong = request.ProductDescriptionLong;
        if (request.ProductImage is not null) existing.ProductImage = request.ProductImage;
        if (request.ProductIsbn is not null) existing.ProductIsbn = request.ProductIsbn;
        if (request.ProductBaseprice != 0) existing.ProductBaseprice = request.ProductBaseprice;
        if (request.ProductOfferprice is not null) existing.ProductOfferprice = request.ProductOfferprice;
        if (request.DiscountPercent is not null) existing.DiscountPercent = request.DiscountPercent;
        if (request.RoyaltyPercent is not null) existing.RoyaltyPercent = request.RoyaltyPercent;
        if (request.ProductOffPriceExpirydate is not null) existing.ProductOffPriceExpirydate = request.ProductOffPriceExpirydate;
        if (request.ProductTypeId is not null) existing.ProductTypeId = request.ProductTypeId;
        if (request.AuthorId is not null) existing.AuthorId = request.AuthorId;
        if (request.PublisherId is not null) existing.PublisherId = request.PublisherId;
        if (request.LanguageId is not null) existing.LanguageId = request.LanguageId;
        if (request.GenereId is not null) existing.GenereId = request.GenereId;
        if (request.RentPerDay is not null) existing.RentPerDay = request.RentPerDay;
        if (request.MinRentDays > 0) existing.MinRentDays = request.MinRentDays;

        // Matches the Java backend's quirk: Rentable/Library are primitive booleans on the
        // incoming request, so they are always overwritten even if the caller didn't intend
        // to change them (there's no way to distinguish "not provided" from "false").
        existing.Rentable = request.Rentable;
        existing.Library = request.Library;

        await _db.SaveChangesAsync();
        var updated = await GetEntityByIdAsync(id);
        return _mapper.Map<ProductDto>(updated);
    }

    public async Task DeleteAsync(int id)
    {
        var product = await _db.Products.FirstOrDefaultAsync(p => p.ProductId == id)
                      ?? throw new ArgumentException($"Product not found: {id}");

        var cover = await _db.ProductCovers.FirstOrDefaultAsync(c => c.ProductId == id);
        if (cover is not null) _db.ProductCovers.Remove(cover);

        var pdf = await _db.ReadBooks.FirstOrDefaultAsync(r => r.ProductId == id);
        if (pdf is not null) _db.ReadBooks.Remove(pdf);

        var attrs = await _db.ProductAttributes.Where(a => a.ProductId == id).ToListAsync();
        _db.ProductAttributes.RemoveRange(attrs);

        var assignments = await _db.BeneficiaryAssignments.Where(a => a.ProductId == id).ToListAsync();
        _db.BeneficiaryAssignments.RemoveRange(assignments);

        var cartLines = await _db.Carts.Where(c => c.ProductId == id).ToListAsync();
        _db.Carts.RemoveRange(cartLines);

        _db.Products.Remove(product);

        try
        {
            await _db.SaveChangesAsync();
        }
        catch (DbUpdateException ex)
        {
            _logger.LogWarning(ex, "Failed to delete product {ProductId} due to FK constraint", id);
            throw new InvalidOperationException($"Can't delete '{product.ProductName}' - it has already been sold, rented, or borrowed.");
        }
    }

    public async Task<ProductDto> UpdateRoyaltyPercentAsync(int id, decimal? royaltyPercent)
    {
        if (royaltyPercent is null)
        {
            throw new ArgumentException("royaltyPercent is required.");
        }

        var product = await _db.Products.FirstOrDefaultAsync(p => p.ProductId == id)
                      ?? throw new ArgumentException($"Product not found: {id}");
        product.RoyaltyPercent = royaltyPercent;
        await _db.SaveChangesAsync();

        var updated = await GetEntityByIdAsync(id);
        return _mapper.Map<ProductDto>(updated);
    }
}
