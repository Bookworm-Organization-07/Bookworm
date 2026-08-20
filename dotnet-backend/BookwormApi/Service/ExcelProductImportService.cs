using System.Text;
using System.Text.RegularExpressions;
using BookwormApi.Data;
using BookwormApi.DTO;
using ClosedXML.Excel;
using Microsoft.AspNetCore.Http;

namespace BookwormApi.Service;

public class ExcelProductImportService : IExcelProductImportService
{
    private const int MinTitleLengthForMatch = 3;
    private static readonly Regex NonAlphaNumeric = new("[^a-zA-Z0-9]", RegexOptions.Compiled);
    private static readonly Regex CamelCaseBoundary = new("(?<=[a-z0-9])(?=[A-Z])", RegexOptions.Compiled);

    private readonly BookwormDbContext _db;
    private readonly IProductRowImportService _rowImportService;
    private readonly IProductCoverService _productCoverService;
    private readonly ILogger<ExcelProductImportService> _logger;

    public ExcelProductImportService(
        BookwormDbContext db,
        IProductRowImportService rowImportService,
        IProductCoverService productCoverService,
        ILogger<ExcelProductImportService> logger)
    {
        _db = db;
        _rowImportService = rowImportService;
        _productCoverService = productCoverService;
        _logger = logger;
    }

    public async Task<BulkUploadResultDto> ImportAsync(IFormFile spreadsheet, List<IFormFile> coverFiles)
    {
        var unmatchedCovers = coverFiles.Where(f => f.ContentType.StartsWith("image/", StringComparison.OrdinalIgnoreCase)).ToList();

        var log = new StringBuilder();
        int total = 0, created = 0, skipped = 0, failed = 0;

        try
        {
            using var stream = spreadsheet.OpenReadStream();
            using var workbook = new XLWorkbook(stream);
            var ws = workbook.Worksheets.First();

            var headerRow = ws.Row(ws.FirstRowUsed()!.RowNumber());
            var lastColumn = ws.LastColumnUsed()!.ColumnNumber();
            var columnMap = new Dictionary<string, int>();
            for (var c = 1; c <= lastColumn; c++)
            {
                var key = headerRow.Cell(c).GetString().Trim().ToLower();
                if (key.Length > 0)
                {
                    columnMap[key] = c;
                }
            }

            var lastRow = ws.LastRowUsed()!.RowNumber();
            for (var r = headerRow.RowNumber() + 1; r <= lastRow; r++)
            {
                var row = ws.Row(r);
                var prodName = GetCellString(row, columnMap, "prod name");
                if (string.IsNullOrWhiteSpace(prodName))
                {
                    continue;
                }

                total++;
                var parsed = new ParsedProductRow(
                    ProdName: prodName,
                    NameEnglish: GetCellString(row, columnMap, "product_name_in_english"),
                    AttributeSet: GetCellString(row, columnMap, "_attribute_set"),
                    TypeLanguageCategory: GetCellString(row, columnMap, "type/language/category"),
                    Availability: GetCellString(row, columnMap, "availability"),
                    Author: GetCellString(row, columnMap, "author"),
                    Publisher: GetCellString(row, columnMap, "publisher"),
                    Description: GetCellString(row, columnMap, "description"),
                    ShortDescription: GetCellString(row, columnMap, "short_description"),
                    IsPackage: GetCellString(row, columnMap, "is_package"),
                    Price: GetCellDecimal(row, columnMap, "price"),
                    SpecialPrice: GetCellDecimal(row, columnMap, "special_price"),
                    CoverId: GetCellString(row, columnMap, "cover_id"));

                try
                {
                    var result = await _rowImportService.ImportRowAsync(parsed);
                    if (result.Status == RowImportStatus.Created) created++;
                    else skipped++;

                    log.AppendLine($"Row {r}: {result.Message}");

                    if (result.ProductId is not null)
                    {
                        var coverNote = await AttachCoverAsync(parsed, result.ProductId.Value, unmatchedCovers);
                        if (!string.IsNullOrEmpty(coverNote))
                        {
                            log.AppendLine(coverNote);
                        }
                    }
                }
                catch (Exception ex)
                {
                    failed++;
                    log.AppendLine($"Row {r}: FAILED - {ex.Message}");
                    _db.ChangeTracker.Clear();
                }
            }
        }
        catch (Exception ex)
        {
            log.AppendLine($"Could not read the file: {ex.Message}");
        }

        return new BulkUploadResultDto
        {
            TotalRows = total,
            CreatedRows = created,
            SkippedRows = skipped,
            FailedRows = failed,
            Log = log.ToString()
        };
    }

    private static string? GetCellString(IXLRow row, Dictionary<string, int> columnMap, string header)
    {
        if (!columnMap.TryGetValue(header, out var col)) return null;
        var value = row.Cell(col).GetString().Trim();
        return value.Length == 0 ? null : value;
    }

    private static decimal? GetCellDecimal(IXLRow row, Dictionary<string, int> columnMap, string header)
    {
        if (!columnMap.TryGetValue(header, out var col)) return null;
        var cell = row.Cell(col);
        if (cell.IsEmpty()) return null;
        return cell.TryGetValue(out double d) ? (decimal)d : null;
    }

    private async Task<string> AttachCoverAsync(ParsedProductRow row, int productId, List<IFormFile> pool)
    {
        var match = FindCoverById(row.CoverId, pool) ?? FindCoverByTitle(row, pool);
        if (match is null)
        {
            return string.Empty;
        }

        pool.Remove(match);

        try
        {
            using var stream = match.OpenReadStream();
            await _productCoverService.SaveCoverAsync(productId, stream, match.ContentType);
            return string.Empty;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to save cover {FileName} for product {ProductId}", match.FileName, productId);
            return $"Cover: '{match.FileName}' could not be saved - {ex.Message}";
        }
    }

    private static IFormFile? FindCoverById(string? coverId, List<IFormFile> pool)
    {
        if (string.IsNullOrWhiteSpace(coverId)) return null;
        var target = coverId.Trim();
        return pool.FirstOrDefault(f => string.Equals(FileStem(f.FileName), target, StringComparison.OrdinalIgnoreCase));
    }

    /// <summary>
    /// Fallback used only when the row has no cover_id (the real "Prod Master Table" export
    /// never has that column - see AttachCoverAsync). Cover filenames in practice are close to,
    /// but not exactly, the title: extra words ("Cover", "-v2", ".web"), no word separators at
    /// all (camelCase, e.g. "BhootBangala01Cover.jpg"), and small spelling differences from the
    /// title (e.g. title "Aanandache Dene" vs file "Aanadache Dene Cover.jpg" - one letter
    /// short). Splitting both into words and comparing word-by-word with a small edit-distance
    /// tolerance catches these without needing an exact title, while still requiring every
    /// meaningful word in the title to be accounted for, so two unrelated titles that merely
    /// share one common word are not treated as a match.
    /// </summary>
    private static IFormFile? FindCoverByTitle(ParsedProductRow row, List<IFormFile> pool)
    {
        var titleWords = SignificantWords(row.NameEnglish);
        if (titleWords.Count == 0)
        {
            titleWords = SignificantWords(row.ProdName);
        }
        if (titleWords.Count == 0)
        {
            return null;
        }

        foreach (var file in pool)
        {
            var fileWords = SignificantWords(FileStem(file.FileName));
            if (fileWords.Count == 0)
            {
                continue;
            }
            if (titleWords.All(titleWord => fileWords.Any(fileWord => IsCloseWordMatch(titleWord, fileWord))))
            {
                return file;
            }
        }
        return null;
    }

    /// <summary>
    /// Splits on both explicit separators (spaces, hyphens, dots, underscores) and camelCase
    /// word boundaries, then drops anything shorter than MinTitleLengthForMatch - short words
    /// ("a", "of", stray digits) are too ambiguous to require or match against safely.
    /// </summary>
    private static List<string> SignificantWords(string? s)
    {
        if (string.IsNullOrWhiteSpace(s)) return new List<string>();
        var spaced = CamelCaseBoundary.Replace(s, " ");
        var raw = NonAlphaNumeric.Replace(spaced, " ").Split(' ', StringSplitOptions.RemoveEmptyEntries);
        return raw.Select(w => w.ToLowerInvariant())
            .Where(w => w.Length >= MinTitleLengthForMatch)
            .ToList();
    }

    /// <summary>Exact match, or within a small edit distance that scales with word length, so a
    /// 4-letter word tolerates less variation than a 10-letter one.</summary>
    private static bool IsCloseWordMatch(string a, string b)
    {
        if (a == b) return true;
        var maxLen = Math.Max(a.Length, b.Length);
        var allowedDistance = maxLen switch
        {
            <= 4 => 0,
            <= 7 => 1,
            _ => 2
        };
        return LevenshteinDistance(a, b) <= allowedDistance;
    }

    private static int LevenshteinDistance(string a, string b)
    {
        var dp = new int[a.Length + 1, b.Length + 1];
        for (var i = 0; i <= a.Length; i++) dp[i, 0] = i;
        for (var j = 0; j <= b.Length; j++) dp[0, j] = j;

        for (var i = 1; i <= a.Length; i++)
        {
            for (var j = 1; j <= b.Length; j++)
            {
                var cost = a[i - 1] == b[j - 1] ? 0 : 1;
                dp[i, j] = Math.Min(Math.Min(dp[i - 1, j] + 1, dp[i, j - 1] + 1), dp[i - 1, j - 1] + cost);
            }
        }
        return dp[a.Length, b.Length];
    }

    /// <summary>
    /// Chrome's webkitdirectory folder-picker upload puts the folder-relative path in the
    /// actual multipart Content-Disposition filename (e.g. "cover_id/201.jpg"), not just the
    /// bare filename shown in the browser's own file list - so IFormFile.FileName can arrive as
    /// "cover_id/201.jpg" even though the picked file is just "201.jpg". Stripping only the
    /// extension left that whole path prefix in place, so cover_id "201" was compared against
    /// "cover_id/201" and never matched - invisible to any test that uploads files individually
    /// (curl, a script) rather than through an actual folder picker. Strip any path prefix
    /// (either separator, in case a teammate is on Windows vs Chrome's own forward-slash) before
    /// stripping the extension, so "201.jpg" and "cover_id/201.jpg" both resolve to "201".
    /// </summary>
    private static string FileStem(string? fileName)
    {
        if (string.IsNullOrEmpty(fileName)) return string.Empty;
        var lastSeparator = fileName.LastIndexOfAny(new[] { '/', '\\' });
        var justName = lastSeparator >= 0 ? fileName[(lastSeparator + 1)..] : fileName;
        var dot = justName.LastIndexOf('.');
        return dot > 0 ? justName[..dot] : justName;
    }
}
