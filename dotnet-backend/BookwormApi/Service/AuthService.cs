using System.Net.Http.Json;
using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;

namespace BookwormApi.Service;

public class GoogleOptions
{
    public string ClientId { get; set; } = string.Empty;
}

public class AuthService : IAuthService
{
    private const string GoogleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private readonly BookwormDbContext _db;
    private readonly IJwtService _jwtService;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly GoogleOptions _googleOptions;
    private readonly ILogger<AuthService> _logger;

    public AuthService(
        BookwormDbContext db,
        IJwtService jwtService,
        IHttpClientFactory httpClientFactory,
        IOptions<GoogleOptions> googleOptions,
        ILogger<AuthService> logger)
    {
        _db = db;
        _jwtService = jwtService;
        _httpClientFactory = httpClientFactory;
        _googleOptions = googleOptions.Value;
        _logger = logger;
    }

    public async Task RegisterAsync(RegisterRequest request)
    {
        if (await _db.Users.AnyAsync(u => u.UserEmail == request.Email))
        {
            throw new InvalidOperationException("That email is already registered.");
        }

        var user = new User
        {
            UserName = request.Name ?? string.Empty,
            UserEmail = request.Email ?? string.Empty,
            UserPhone = request.Phone,
            UserAddress = request.Address,
            UserPassword = BCrypt.Net.BCrypt.HashPassword(request.Password ?? string.Empty),
            JoinDate = DateOnly.FromDateTime(DateTime.Today),
            Admin = false
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();
        _logger.LogInformation("Registered new user {Email}", user.UserEmail);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.UserEmail == request.Email)
                   ?? throw new ArgumentException("Invalid email or password.");

        if (!BCrypt.Net.BCrypt.Verify(request.Password ?? string.Empty, user.UserPassword))
        {
            throw new ArgumentException("Invalid email or password.");
        }

        return BuildAuthResponse(user);
    }

    public async Task<AuthResponse> LoginWithGoogleAsync(string? idToken)
    {
        if (string.IsNullOrWhiteSpace(idToken))
        {
            throw new ArgumentException("No Google credential was provided.");
        }

        GoogleTokenInfo? tokenInfo;
        try
        {
            var client = _httpClientFactory.CreateClient("Google");
            var response = await client.GetAsync(GoogleTokenInfoUrl + Uri.EscapeDataString(idToken));
            if (!response.IsSuccessStatusCode)
            {
                throw new ArgumentException("Could not verify this Google sign-in.");
            }
            tokenInfo = await response.Content.ReadFromJsonAsync<GoogleTokenInfo>();
        }
        catch (ArgumentException)
        {
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Google tokeninfo call failed");
            throw new ArgumentException("Could not verify this Google sign-in.");
        }

        if (tokenInfo is null || tokenInfo.Email is null)
        {
            throw new ArgumentException("Could not verify this Google sign-in.");
        }

        if (!string.Equals(_googleOptions.ClientId, tokenInfo.Aud, StringComparison.Ordinal))
        {
            throw new ArgumentException("This Google sign-in was not issued for this app.");
        }

        if (!tokenInfo.IsEmailVerified)
        {
            throw new ArgumentException("This Google account's email address is not verified.");
        }

        var user = await _db.Users.FirstOrDefaultAsync(u => u.UserEmail == tokenInfo.Email);
        if (user is null)
        {
            user = new User
            {
                UserName = tokenInfo.Name ?? tokenInfo.Email,
                UserEmail = tokenInfo.Email,
                UserPassword = BCrypt.Net.BCrypt.HashPassword(Guid.NewGuid().ToString()),
                JoinDate = DateOnly.FromDateTime(DateTime.Today),
                Admin = false
            };
            _db.Users.Add(user);
            await _db.SaveChangesAsync();
            _logger.LogInformation("Auto-registered new user via Google sign-in: {Email}", user.UserEmail);
        }

        return BuildAuthResponse(user);
    }

    public async Task<AuthResponse> RefreshAsync(string? refreshToken)
    {
        if (string.IsNullOrWhiteSpace(refreshToken))
        {
            throw new ArgumentException("Refresh token is required.");
        }

        // ValidateRefreshToken already confirms the signature, that it
        // hasn't expired (7 days), and that it really is a refresh token -
        // not just any valid JWT.
        var email = _jwtService.ValidateRefreshToken(refreshToken)
                    ?? throw new ArgumentException("Refresh token is invalid or has expired. Please log in again.");

        var user = await _db.Users.FirstOrDefaultAsync(u => u.UserEmail == email)
                   ?? throw new ArgumentException("Refresh token is invalid or has expired. Please log in again.");

        // Re-reads the user's admin flag from the database rather than
        // trusting anything from the old token, in case it changed since
        // they first logged in. The refresh token itself is reused as-is
        // (no rotation) - simplest option, and fine here since it still
        // naturally expires after 7 days either way.
        var newAccessToken = _jwtService.GenerateAccessToken(user.UserEmail, user.Admin);
        return new AuthResponse(newAccessToken, refreshToken, user.UserId, user.UserName, user.Admin);
    }

    private AuthResponse BuildAuthResponse(User user)
    {
        var accessToken = _jwtService.GenerateAccessToken(user.UserEmail, user.Admin);
        var refreshToken = _jwtService.GenerateRefreshToken(user.UserEmail);
        return new AuthResponse(accessToken, refreshToken, user.UserId, user.UserName, user.Admin);
    }
}
