namespace BookwormApi.Service;

public record ParsedProductRow(
    string? ProdName,
    string? NameEnglish,
    string? AttributeSet,
    string? TypeLanguageCategory,
    string? Availability,
    string? Author,
    string? Publisher,
    string? Description,
    string? ShortDescription,
    string? IsPackage,
    decimal? Price,
    decimal? SpecialPrice,
    string? CoverId);

public enum RowImportStatus
{
    Created,
    Skipped
}

public record RowImportResult(RowImportStatus Status, string Message, int? ProductId);
