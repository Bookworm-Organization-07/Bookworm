using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/books")]
[Authorize(Roles = "ROLE_ADMIN")]
public class ReadBookController : ControllerBase
{
    private readonly IReadBookService _readBookService;

    public ReadBookController(IReadBookService readBookService) => _readBookService = readBookService;

    [HttpPost("{productId:int}/upload")]
    [RequestSizeLimit(25_000_000)]
    public async Task<IActionResult> Upload(int productId, IFormFile file)
    {
        using var stream = file.OpenReadStream();
        await _readBookService.SavePdfAsync(productId, stream, file.FileName);
        return Ok(new { message = "Book PDF uploaded." });
    }
}
