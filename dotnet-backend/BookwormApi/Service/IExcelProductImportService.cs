using BookwormApi.DTO;
using Microsoft.AspNetCore.Http;

namespace BookwormApi.Service;

public interface IExcelProductImportService
{
    Task<BulkUploadResultDto> ImportAsync(IFormFile spreadsheet, List<IFormFile> coverFiles);
}
