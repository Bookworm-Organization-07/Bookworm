using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/transactions")]
[Authorize]
public class TransactionController : ControllerBase
{
    private readonly ITransactionService _transactionService;
    private readonly ICurrentUserService _currentUserService;

    public TransactionController(ITransactionService transactionService, ICurrentUserService currentUserService)
    {
        _transactionService = transactionService;
        _currentUserService = currentUserService;
    }

    [HttpGet("mine")]
    public async Task<IActionResult> Mine()
    {
        var user = await _currentUserService.RequireAsync();
        return Ok(await _transactionService.GetMineAsync(user));
    }

    [HttpGet("{transactionId:long}/items")]
    public async Task<IActionResult> Items(long transactionId)
    {
        var user = await _currentUserService.RequireAsync();
        return Ok(await _transactionService.GetItemsAsync(user, transactionId));
    }
}
