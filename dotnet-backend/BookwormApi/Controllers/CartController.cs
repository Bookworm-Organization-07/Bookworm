using BookwormApi.DTO;
using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/cart")]
[Authorize]
public class CartController : ControllerBase
{
    private readonly ICartService _cartService;
    private readonly ICurrentUserService _currentUserService;

    public CartController(ICartService cartService, ICurrentUserService currentUserService)
    {
        _cartService = cartService;
        _currentUserService = currentUserService;
    }

    [HttpGet]
    public async Task<IActionResult> GetCart()
    {
        var user = await _currentUserService.RequireAsync();
        return Ok(await _cartService.GetCartAsync(user));
    }

    [HttpPost("add")]
    public async Task<IActionResult> Add([FromBody] AddToCartRequest request)
    {
        var user = await _currentUserService.RequireAsync();
        var result = await _cartService.AddToCartAsync(user, request.ProductId!.Value, request.Qty, request.RentDays);
        return Ok(result);
    }

    [HttpPut("{cartId:int}")]
    public async Task<IActionResult> UpdateQty(int cartId, [FromBody] UpdateCartQtyRequest request)
    {
        var user = await _currentUserService.RequireAsync();
        var result = await _cartService.UpdateQtyAsync(user, cartId, request.Qty);
        return Ok(result);
    }

    [HttpDelete("remove/{cartId:int}")]
    public async Task<IActionResult> Remove(int cartId)
    {
        var user = await _currentUserService.RequireAsync();
        await _cartService.RemoveItemAsync(user, cartId);
        return NoContent();
    }

    [HttpDelete]
    public async Task<IActionResult> Clear()
    {
        var user = await _currentUserService.RequireAsync();
        await _cartService.ClearCartAsync(user);
        return NoContent();
    }
}
