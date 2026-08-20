using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/royalty-ledger")]
[Authorize(Roles = "ROLE_ADMIN")]
public class RoyaltyLedgerController : ControllerBase
{
    private readonly IRoyaltyLedgerService _service;

    public RoyaltyLedgerController(IRoyaltyLedgerService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetLedgerAsync());
}
