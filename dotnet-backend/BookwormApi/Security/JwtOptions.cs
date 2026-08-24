namespace BookwormApi.Security;

public class JwtOptions
{
    public string Secret { get; set; } = null!;

    // How long a freshly-logged-in user can call the API before their
    // access token stops working and the frontend has to use the refresh
    // token to get a new one. Kept short (15 min) so a leaked access token
    // is only useful for a little while.
    public int AccessTokenExpiryMinutes { get; set; } = 15;

    // How long the user stays "logged in" without re-entering a password.
    // Kept much longer (7 days) than the access token since it is only ever
    // sent to POST /api/auth/refresh, never to a regular API call - see
    // JwtService.GenerateRefreshToken.
    public int RefreshTokenExpiryDays { get; set; } = 7;
}
