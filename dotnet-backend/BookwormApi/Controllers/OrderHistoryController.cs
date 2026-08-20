using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/orders")]
[Authorize]
public class OrderHistoryController : ControllerBase
{
    private readonly IOrderHistoryService _orderHistoryService;
    private readonly ICurrentUserService _currentUserService;

    public OrderHistoryController(IOrderHistoryService orderHistoryService, ICurrentUserService currentUserService)
    {
        _orderHistoryService = orderHistoryService;
        _currentUserService = currentUserService;
    }

    [HttpGet("history/mine")]
    public async Task<IActionResult> Mine()
    {
        var user = await _currentUserService.RequireAsync();
        return Ok(await _orderHistoryService.GetMineAsync(user));
    }

    [HttpGet("history")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> All() => Ok(await _orderHistoryService.GetAllAsync());
}
