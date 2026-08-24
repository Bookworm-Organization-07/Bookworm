using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace BookwormApi.DTO;

public class LoginRequest
{
    [Required(ErrorMessage = "Email is required.")]
    [EmailAddress(ErrorMessage = "Enter a valid email address.")]
    public string? Email { get; set; }

    [Required(ErrorMessage = "Password is required.")]
    public string? Password { get; set; }
}

public class RegisterRequest
{
    [Required(ErrorMessage = "Full name is required.")]
    public string? Name { get; set; }

    [Required(ErrorMessage = "Email is required.")]
    [EmailAddress(ErrorMessage = "Enter a valid email address.")]
    public string? Email { get; set; }

    public string? Phone { get; set; }
    public string? Address { get; set; }

    [Required(ErrorMessage = "Password is required.")]
    [MinLength(6, ErrorMessage = "Password must be at least 6 characters.")]
    public string? Password { get; set; }
}

public class AuthResponse
{
    public string Token { get; }

    // Sent to POST /api/auth/refresh once the (short-lived) access token
    // above expires, to get a new one without asking for the password again.
    public string RefreshToken { get; }
    public int UserId { get; }
    public string UserName { get; }
    public bool Admin { get; }

    public AuthResponse(string token, string refreshToken, int userId, string userName, bool admin)
    {
        Token = token;
        RefreshToken = refreshToken;
        UserId = userId;
        UserName = userName;
        Admin = admin;
    }
}

public class RefreshRequest
{
    [Required(ErrorMessage = "Refresh token is required.")]
    public string? RefreshToken { get; set; }
}

public class GoogleLoginRequest
{
    public string? IdToken { get; set; }
}

public class GoogleTokenInfo
{
    public string? Aud { get; set; }
    public string? Email { get; set; }

    [JsonPropertyName("email_verified")]
    public string? EmailVerifiedRaw { get; set; }

    public string? Name { get; set; }

    [JsonIgnore]
    public bool IsEmailVerified => string.Equals(EmailVerifiedRaw, "true", StringComparison.OrdinalIgnoreCase);
}
