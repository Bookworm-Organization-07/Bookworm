using BookwormApi.DTO;
using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/auth")]
[AllowAnonymous]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;
    private readonly GoogleOptions _googleOptions;

    public AuthController(IAuthService authService, IOptions<GoogleOptions> googleOptions)
    {
        _authService = authService;
        _googleOptions = googleOptions.Value;
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        await _authService.RegisterAsync(request);
        return Ok(new { message = "Registered successfully." });
    }

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponse>> Login([FromBody] LoginRequest request)
    {
        var result = await _authService.LoginAsync(request);
        return Ok(result);
    }

    [HttpGet("google/client-id")]
    public IActionResult GoogleClientId()
    {
        return Ok(new { clientId = _googleOptions.ClientId });
    }

    [HttpPost("google")]
    public async Task<ActionResult<AuthResponse>> GoogleLogin([FromBody] GoogleLoginRequest request)
    {
        var result = await _authService.LoginWithGoogleAsync(request.IdToken);
        return Ok(result);
    }

    // Called once the access token from login/google (15 min) has expired,
    // to get a new one without the user having to log in again. Takes the
    // refresh token (7 days) from the earlier login response.
    [HttpPost("refresh")]
    public async Task<ActionResult<AuthResponse>> Refresh([FromBody] RefreshRequest request)
    {
        var result = await _authService.RefreshAsync(request.RefreshToken);
        return Ok(result);
    }
}
