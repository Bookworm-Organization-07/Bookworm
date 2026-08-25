namespace BookwormApi.Service;

public interface IProductRowImportService
{
    Task<RowImportResult> ImportRowAsync(ParsedProductRow row);
}
