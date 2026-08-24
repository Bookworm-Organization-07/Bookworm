namespace BookwormApi.Service;

public interface IJwtService
{
    string GenerateAccessToken(string email, bool isAdmin);

    string GenerateRefreshToken(string email);

    // Checks the refresh token's signature, expiry, and that it really is a
    // refresh token (not an access token someone passed in by mistake).
    // Returns the user's email if all of that checks out, or null otherwise -
    // callers just treat null as "reject this refresh attempt".
    string? ValidateRefreshToken(string refreshToken);
}
