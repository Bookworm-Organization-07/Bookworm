using BookwormApi.DTO;
using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/library/checkout")]
[Authorize]
public class LibraryCheckoutController : ControllerBase
{
    private readonly ILibraryCheckoutService _libraryCheckoutService;
    private readonly ICurrentUserService _currentUserService;

    public LibraryCheckoutController(ILibraryCheckoutService libraryCheckoutService, ICurrentUserService currentUserService)
    {
        _libraryCheckoutService = libraryCheckoutService;
        _currentUserService = currentUserService;
    }

    [HttpPost]
    public async Task<IActionResult> Checkout([FromBody] LibraryCheckoutRequest request)
    {
        var user = await _currentUserService.RequireAsync();
        await _libraryCheckoutService.CheckoutAsync(user, request);
        return Ok(new { message = "Library checkout successful." });
    }
}
