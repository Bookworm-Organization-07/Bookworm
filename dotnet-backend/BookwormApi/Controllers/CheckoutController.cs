using BookwormApi.DTO;
using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/checkout")]
[Authorize]
public class CheckoutController : ControllerBase
{
    private readonly ICheckoutService _checkoutService;
    private readonly ICurrentUserService _currentUserService;

    public CheckoutController(ICheckoutService checkoutService, ICurrentUserService currentUserService)
    {
        _checkoutService = checkoutService;
        _currentUserService = currentUserService;
    }

    [HttpPost]
    public async Task<IActionResult> Checkout()
    {
        var user = await _currentUserService.RequireAsync();
        var transactions = await _checkoutService.CheckoutAsync(user);

        var response = new CheckoutResponseDto
        {
            Transactions = transactions.Select(t => new CheckoutTransactionSummary
            {
                TransactionId = t.TransactionId,
                TotalAmount = t.TotalAmount,
                TransactionType = t.TransactionType?.ToString()
            }).ToList()
        };

        return Ok(response);
    }
}
