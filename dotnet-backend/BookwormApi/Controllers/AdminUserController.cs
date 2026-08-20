using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/admin/users")]
[Authorize(Roles = "ROLE_ADMIN")]
public class AdminUserController : ControllerBase
{
    private readonly IAdminUserService _service;
    private readonly ICurrentUserService _currentUserService;

    public AdminUserController(IAdminUserService service, ICurrentUserService currentUserService)
    {
        _service = service;
        _currentUserService = currentUserService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllUsersAsync());

    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id)
    {
        var admin = await _currentUserService.RequireAsync();
        await _service.DeleteUserAsync(id, admin);
        return NoContent();
    }
}
