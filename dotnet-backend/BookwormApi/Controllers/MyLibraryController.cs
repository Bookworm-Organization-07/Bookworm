using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/my-library")]
[Authorize]
public class MyLibraryController : ControllerBase
{
    private readonly IMyLibraryService _myLibraryService;
    private readonly ICurrentUserService _currentUserService;

    public MyLibraryController(IMyLibraryService myLibraryService, ICurrentUserService currentUserService)
    {
        _myLibraryService = myLibraryService;
        _currentUserService = currentUserService;
    }

    [HttpGet]
    public async Task<IActionResult> GetMyLibrary()
    {
        var user = await _currentUserService.RequireAsync();
        return Ok(await _myLibraryService.GetUserLibraryAsync(user));
    }

    [HttpGet("read/{productId:int}")]
    public async Task<IActionResult> ReadBook(int productId)
    {
        var user = await _currentUserService.RequireAsync();
        var (data, _) = await _myLibraryService.ReadLibraryBookAsync(user, productId);
        return File(data, "application/pdf");
    }
}
