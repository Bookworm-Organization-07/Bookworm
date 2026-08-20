using BookwormApi.DTO;
using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/products")]
public class ProductController : ControllerBase
{
    private readonly IProductService _productService;
    private readonly IProductCoverService _productCoverService;
    private readonly IBeneficiaryAssignmentService _beneficiaryAssignmentService;
    private readonly IExcelProductImportService _excelImportService;
    private readonly IProductRowImportService _rowImportService;
    private readonly IBookDescriptionAiService _aiService;

    public ProductController(
        IProductService productService,
        IProductCoverService productCoverService,
        IBeneficiaryAssignmentService beneficiaryAssignmentService,
        IExcelProductImportService excelImportService,
        IProductRowImportService rowImportService,
        IBookDescriptionAiService aiService)
    {
        _productService = productService;
        _productCoverService = productCoverService;
        _beneficiaryAssignmentService = beneficiaryAssignmentService;
        _excelImportService = excelImportService;
        _rowImportService = rowImportService;
        _aiService = aiService;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<List<ProductDto>>> GetAll() => Ok(await _productService.GetAllAsync());

    [HttpGet("{id:int}")]
    [AllowAnonymous]
    public async Task<ActionResult<ProductDto>> GetById(int id) => Ok(await _productService.GetByIdAsync(id));

    [HttpGet("search")]
    [AllowAnonymous]
    public async Task<ActionResult<List<ProductDto>>> Search([FromQuery] string? name, [FromQuery] int? limit)
        => Ok(await _productService.SearchAsync(name, limit));

    [HttpGet("library")]
    [AllowAnonymous]
    public async Task<ActionResult<List<ProductDto>>> Library([FromQuery] string? genere, [FromQuery] string? language)
        => Ok(await _productService.GetLibraryProductsAsync(genere, language));

    [HttpGet("filter")]
    [AllowAnonymous]
    public async Task<ActionResult<List<ProductDto>>> Filter([FromQuery] string? genere, [FromQuery] string? language)
        => Ok(await _productService.FilterProductsAsync(genere, language));

    [HttpPost]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<ActionResult<ProductDto>> Create([FromBody] ProductRequestDto request)
    {
        var created = await _productService.CreateAsync(request);
        return StatusCode(StatusCodes.Status201Created, created);
    }

    [HttpPatch("{id:int}")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<ActionResult<ProductDto>> Update(int id, [FromBody] ProductRequestDto request)
        => Ok(await _productService.UpdateAsync(id, request));

    [HttpDelete("{id:int}")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> Delete(int id)
    {
        await _productService.DeleteAsync(id);
        return NoContent();
    }

    [HttpPatch("{id:int}/royalty")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<ActionResult<ProductDto>> UpdateRoyalty(int id, [FromBody] UpdateRoyaltyRequest request)
        => Ok(await _productService.UpdateRoyaltyPercentAsync(id, request.RoyaltyPercent));

    [HttpGet("{id:int}/beneficiaries")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> GetBeneficiaries(int id)
    {
        var beneficiaries = await _beneficiaryAssignmentService.GetAssignmentsAsync(id);
        return Ok(beneficiaries);
    }

    [HttpPost("{id:int}/beneficiaries/{beneficiaryId:int}")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> AssignBeneficiary(int id, int beneficiaryId)
    {
        var product = await _productService.GetEntityByIdAsync(id);
        await _beneficiaryAssignmentService.AssignAsync(product, beneficiaryId);
        return NoContent();
    }

    [HttpDelete("{id:int}/beneficiaries/{beneficiaryId:int}")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> UnassignBeneficiary(int id, int beneficiaryId)
    {
        var product = await _productService.GetEntityByIdAsync(id);
        await _beneficiaryAssignmentService.UnassignAsync(product, beneficiaryId);
        return NoContent();
    }

    [HttpPost("quick-add")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<ActionResult<QuickAddResultDto>> QuickAdd([FromBody] ManualProductRequest request)
    {
        var availability = (request.Rentable ? "rent " : string.Empty) + (request.Library ? "package" : string.Empty);
        var parsed = new ParsedProductRow(
            ProdName: request.ProdName,
            NameEnglish: request.NameEnglish,
            AttributeSet: null,
            TypeLanguageCategory: $"{request.Type}/{request.Language}/{request.Genre}",
            Availability: availability,
            Author: request.Author,
            Publisher: request.Publisher,
            Description: request.Description,
            ShortDescription: request.ShortDescription,
            IsPackage: null,
            Price: request.Price,
            SpecialPrice: request.SpecialPrice,
            CoverId: null);

        var result = await _rowImportService.ImportRowAsync(parsed);
        return Ok(new QuickAddResultDto { Message = result.Message, ProductId = result.ProductId ?? 0 });
    }

    [HttpPost("bulk-upload")]
    [Authorize(Roles = "ROLE_ADMIN")]
    [RequestSizeLimit(300_000_000)]
    public async Task<ActionResult<BulkUploadResultDto>> BulkUpload(IFormFile file, [FromForm] List<IFormFile>? coverFiles)
    {
        var result = await _excelImportService.ImportAsync(file, coverFiles ?? new List<IFormFile>());
        return Ok(result);
    }

    [HttpPost("{id:int}/cover")]
    [Authorize(Roles = "ROLE_ADMIN")]
    [RequestSizeLimit(25_000_000)]
    public async Task<IActionResult> UploadCover(int id, IFormFile file)
    {
        using var stream = file.OpenReadStream();
        await _productCoverService.SaveCoverAsync(id, stream, file.ContentType);
        return Ok(new { message = "Cover updated." });
    }

    [HttpGet("{id:int}/cover")]
    [AllowAnonymous]
    public async Task<IActionResult> GetCover(int id)
    {
        var (data, contentType) = await _productCoverService.GetCoverAsync(id);
        return File(data, contentType);
    }

    [HttpPost("{id:int}/ai-short-description")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> AiShortDescription(int id)
    {
        if (!_aiService.IsConfigured)
        {
            return StatusCode(StatusCodes.Status503ServiceUnavailable, new { message = "AI assistance is not configured." });
        }

        var product = await _productService.GetEntityByIdAsync(id);
        var draft = await _aiService.DraftShortDescriptionAsync(product.ProductDescriptionLong ?? product.ProductName);
        return Ok(new { shortDescription = draft });
    }
}
