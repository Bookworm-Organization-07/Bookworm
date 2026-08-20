using System.Text.RegularExpressions;
using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class ProductRowImportService : IProductRowImportService
{
    private const decimal RentPricePercentOfSale = 0.10m;
    private const int DefaultMinRentDays = 1;
    private const int OfferValidYears = 10;

    private static readonly Dictionary<string, string> LanguageTranslation = new()
    {
        ["मराठी"] = "Marathi",
        ["हिंदी"] = "Hindi",
        ["कोंकणी"] = "Konkani",
        ["english"] = "English"
    };

    private static readonly Dictionary<string, string> ProductTypeTranslation = new()
    {
        ["e-book"] = "eBook",
        ["e-audio books"] = "Audiobook",
        ["e-comics"] = "Comics"
    };

    private readonly BookwormDbContext _db;

    public ProductRowImportService(BookwormDbContext db)
    {
        _db = db;
    }

    public async Task<RowImportResult> ImportRowAsync(ParsedProductRow row)
    {
        var prodName = row.ProdName ?? string.Empty;

        var isPackageRow = string.Equals(row.AttributeSet, "Package", StringComparison.OrdinalIgnoreCase) ||
                            string.Equals(row.IsPackage, "Yes", StringComparison.OrdinalIgnoreCase);
        if (isPackageRow)
        {
            return new RowImportResult(RowImportStatus.Skipped,
                $"Skipped - '{prodName}' is a library package, not a book. Add packages from Admin > Products instead.", null);
        }

        var existing = await _db.Products.FirstOrDefaultAsync(p => p.ProductName.ToLower() == prodName.ToLower());
        if (existing is not null)
        {
            return new RowImportResult(RowImportStatus.Skipped, $"Skipped - '{prodName}' is already in the catalogue.", existing.ProductId);
        }

        if (row.Price is null)
        {
            throw new ArgumentException($"No price given for '{prodName}'");
        }

        var segments = SplitTypeLanguageCategory(row.TypeLanguageCategory);

        var product = new Product
        {
            ProductName = Truncate(prodName, 150) ?? string.Empty,
            ProductNameEnglish = Truncate(row.NameEnglish, 150),
            ProductBaseprice = row.Price.Value
        };

        product.ProductTypeId = (await ResolveProductTypeAsync(segments.Length > 0 ? segments[0] : "eBook")).TypeId;
        product.LanguageId = (await ResolveLanguageAsync(segments.Length > 1 ? segments[1] : "English")).LanguageId;
        product.GenereId = (await ResolveGenereAsync(segments.Length > 2 ? segments[2] : "General")).GenereId;

        if (row.Author is not null && !string.Equals(row.Author, "package", StringComparison.OrdinalIgnoreCase))
        {
            product.AuthorId = (await ResolveAuthorAsync(row.Author)).AuthorId;
        }
        if (row.Publisher is not null)
        {
            product.PublisherId = (await ResolvePublisherAsync(row.Publisher)).PublisherId;
        }

        ApplySpecialPrice(product, row);

        product.ProductDescriptionLong = StripHtml(row.Description);
        product.ProductDescriptionShort = Truncate(StripHtml(row.ShortDescription), 255);
        product.ProductIsbn = $"BU-{Guid.NewGuid():N}"[..15].ToUpperInvariant();

        ApplyAvailability(product, row.Availability);

        _db.Products.Add(product);
        await _db.SaveChangesAsync();

        return new RowImportResult(RowImportStatus.Created, $"Created product #{product.ProductId} '{prodName}'", product.ProductId);
    }

    private static string[] SplitTypeLanguageCategory(string? raw)
    {
        if (string.IsNullOrWhiteSpace(raw)) return Array.Empty<string>();
        return raw.Split('/').Select(s => s.Trim()).ToArray();
    }

    private async Task<ProductType> ResolveProductTypeAsync(string raw)
    {
        var translated = ProductTypeTranslation.GetValueOrDefault(raw.Trim().ToLower(), raw.Trim());
        var existing = await _db.ProductTypes.FirstOrDefaultAsync(t => t.TypeDesc.ToLower() == translated.ToLower());
        if (existing is not null) return existing;

        var created = new ProductType { TypeDesc = Truncate(translated, 50) ?? translated };
        _db.ProductTypes.Add(created);
        await _db.SaveChangesAsync();
        return created;
    }

    private async Task<Language> ResolveLanguageAsync(string raw)
    {
        var translated = LanguageTranslation.GetValueOrDefault(raw.Trim().ToLower(), raw.Trim());
        var existing = await _db.Languages.FirstOrDefaultAsync(l => l.LanguageDesc.ToLower() == translated.ToLower());
        if (existing is not null) return existing;

        var created = new Language { LanguageDesc = Truncate(translated, 50) ?? translated };
        _db.Languages.Add(created);
        await _db.SaveChangesAsync();
        return created;
    }

    private async Task<Genere> ResolveGenereAsync(string raw)
    {
        var trimmed = raw.Trim();
        var existing = await _db.Generes.FirstOrDefaultAsync(g => g.GenereDesc.ToLower() == trimmed.ToLower());
        if (existing is not null) return existing;

        var created = new Genere { GenereDesc = Truncate(trimmed, 50) ?? trimmed };
        _db.Generes.Add(created);
        await _db.SaveChangesAsync();
        return created;
    }

    private async Task<Author> ResolveAuthorAsync(string raw)
    {
        var trimmed = raw.Trim();
        var existing = await _db.Authors.FirstOrDefaultAsync(a => a.Name.ToLower() == trimmed.ToLower());
        if (existing is not null) return existing;

        var created = new Author { Name = Truncate(trimmed, 100) ?? trimmed };
        _db.Authors.Add(created);
        await _db.SaveChangesAsync();
        return created;
    }

    private async Task<Publisher> ResolvePublisherAsync(string raw)
    {
        var trimmed = raw.Trim();
        var existing = await _db.Publishers.FirstOrDefaultAsync(p => p.Name.ToLower() == trimmed.ToLower());
        if (existing is not null) return existing;

        var created = new Publisher
        {
            Name = Truncate(trimmed, 100) ?? trimmed,
            Email = $"bulk-upload+{Guid.NewGuid()}@placeholder.local"
        };
        _db.Publishers.Add(created);
        await _db.SaveChangesAsync();
        return created;
    }

    private static void ApplySpecialPrice(Product product, ParsedProductRow row)
    {
        if (row.SpecialPrice is null || row.SpecialPrice >= row.Price)
        {
            return;
        }
        product.ProductOfferprice = row.SpecialPrice;
        product.ProductOffPriceExpirydate = DateOnly.FromDateTime(DateTime.Today).AddYears(OfferValidYears);
    }

    private static void ApplyAvailability(Product product, string? availability)
    {
        var text = (availability ?? string.Empty).ToLower();
        product.Rentable = text.Contains("rent");
        product.Library = text.Contains("package");

        if (product.Rentable)
        {
            var basis = product.ProductOfferprice ?? product.ProductBaseprice;
            product.RentPerDay = Math.Round(basis * RentPricePercentOfSale, 2, MidpointRounding.AwayFromZero);
            product.MinRentDays = DefaultMinRentDays;
        }
    }

    private static readonly Regex BrTagRegex = new("<br\\s*/?>", RegexOptions.IgnoreCase | RegexOptions.Compiled);
    private static readonly Regex AnyTagRegex = new("<[^>]*>", RegexOptions.Compiled);
    private static readonly Regex SpaceRunRegex = new("[ \t]+", RegexOptions.Compiled);
    private static readonly Regex NewlineRunRegex = new("\n{3,}", RegexOptions.Compiled);

    internal static string? StripHtml(string? html)
    {
        if (string.IsNullOrEmpty(html)) return html;

        var text = BrTagRegex.Replace(html, "\n");
        text = text.Replace("</p>", "\n\n", StringComparison.OrdinalIgnoreCase);
        text = AnyTagRegex.Replace(text, "");

        text = text.Replace("&nbsp;", " ")
            .Replace("&ldquo;", "“")
            .Replace("&rdquo;", "”")
            .Replace("&lsquo;", "‘")
            .Replace("&rsquo;", "’")
            .Replace("&mdash;", "—")
            .Replace("&ndash;", "–")
            .Replace("&hellip;", "…")
            .Replace("&quot;", "\"")
            .Replace("&#39;", "'")
            .Replace("&amp;", "&")
            .Replace("&lt;", "<")
            .Replace("&gt;", ">");

        text = SpaceRunRegex.Replace(text, " ");
        text = NewlineRunRegex.Replace(text, "\n\n");

        return text.Trim();
    }

    internal static string? Truncate(string? value, int maxLength)
    {
        if (value is null) return null;
        return value.Length > maxLength ? value[..maxLength] : value;
    }
}
