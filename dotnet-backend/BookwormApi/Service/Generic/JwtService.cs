using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using BookwormApi.Security;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace BookwormApi.Service;

public class JwtService : IJwtService
{
    // Marks which kind of token this is, via a "token_type" claim. Both
    // access and refresh tokens are signed with the same secret, so without
    // this a refresh token would work as a Bearer access token too - see
    // ValidateRefreshToken below and the OnTokenValidated check in
    // Program.cs, which both check this claim.
    private const string AccessTokenType = "access";
    private const string RefreshTokenType = "refresh";
    private const string TokenTypeClaim = "token_type";

    private readonly JwtOptions _options;

    public JwtService(IOptions<JwtOptions> options)
    {
        _options = options.Value;
    }

    public string GenerateAccessToken(string email, bool isAdmin)
    {
        var role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        var claims = new List<Claim> { new("role", role), new(TokenTypeClaim, AccessTokenType) };
        return BuildToken(email, claims, TimeSpan.FromMinutes(_options.AccessTokenExpiryMinutes));
    }

    public string GenerateRefreshToken(string email)
    {
        // No "role" claim here on purpose - a refresh token is only ever
        // exchanged for a fresh access token (AuthService.RefreshAsync looks
        // the user up again to get their current role), it never grants API
        // access by itself.
        var claims = new List<Claim> { new(TokenTypeClaim, RefreshTokenType) };
        return BuildToken(email, claims, TimeSpan.FromDays(_options.RefreshTokenExpiryDays));
    }

    public string? ValidateRefreshToken(string refreshToken)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_options.Secret));
        var validationParameters = new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = key
        };

        try
        {
            var principal = new JwtSecurityTokenHandler().ValidateToken(refreshToken, validationParameters, out _);
            var tokenType = principal.FindFirst(TokenTypeClaim)?.Value;
            if (tokenType != RefreshTokenType)
            {
                return null;
            }
            return principal.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;
        }
        catch
        {
            // Expired, tampered-with, or just malformed - all treated the
            // same way: not a valid refresh token.
            return null;
        }
    }

    private string BuildToken(string email, List<Claim> extraClaims, TimeSpan expiresIn)
    {
        var now = DateTimeOffset.UtcNow;
        var claims = new List<Claim>
        {
            new(JwtRegisteredClaimNames.Sub, email),
            new(JwtRegisteredClaimNames.Iat, now.ToUnixTimeSeconds().ToString(), ClaimValueTypes.Integer64)
        };
        claims.AddRange(extraClaims);

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_options.Secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var token = new JwtSecurityToken(
            claims: claims,
            expires: now.UtcDateTime.Add(expiresIn),
            signingCredentials: creds);

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}
