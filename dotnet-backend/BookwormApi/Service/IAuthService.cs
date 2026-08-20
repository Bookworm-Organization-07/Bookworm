using BookwormApi.DTO;

namespace BookwormApi.Service;

public interface IAuthService
{
    Task RegisterAsync(RegisterRequest request);
    Task<AuthResponse> LoginAsync(LoginRequest request);
    Task<AuthResponse> LoginWithGoogleAsync(string? idToken);
    Task<AuthResponse> RefreshAsync(string? refreshToken);
}
