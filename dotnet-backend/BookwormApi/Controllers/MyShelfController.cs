using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/shelf")]
[Authorize]
public class MyShelfController : ControllerBase
{
    private readonly IShelfService _shelfService;
    private readonly ICurrentUserService _currentUserService;

    public MyShelfController(IShelfService shelfService, ICurrentUserService currentUserService)
    {
        _shelfService = shelfService;
        _currentUserService = currentUserService;
    }

    [HttpGet]
    public async Task<IActionResult> GetShelf()
    {
        var user = await _currentUserService.RequireAsync();
        return Ok(await _shelfService.GetShelfAsync(user));
    }

    [HttpDelete("{shelfId:int}")]
    public async Task<IActionResult> Remove(int shelfId)
    {
        var user = await _currentUserService.RequireAsync();
        await _shelfService.RemoveAsync(user, shelfId);
        return NoContent();
    }
}
