using BookwormApi.Data;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class CurrentUserService : ICurrentUserService
{
    private readonly IHttpContextAccessor _httpContextAccessor;
    private readonly BookwormDbContext _db;

    public CurrentUserService(IHttpContextAccessor httpContextAccessor, BookwormDbContext db)
    {
        _httpContextAccessor = httpContextAccessor;
        _db = db;
    }

    public async Task<User> RequireAsync()
    {
        var principal = _httpContextAccessor.HttpContext?.User;
        var email = principal?.Identity?.IsAuthenticated == true ? principal.Identity!.Name : null;
        if (string.IsNullOrEmpty(email))
        {
            throw new InvalidOperationException("Not signed in");
        }

        var user = await _db.Users.FirstOrDefaultAsync(u => u.UserEmail == email);
        if (user is null)
        {
            throw new InvalidOperationException("Signed-in user no longer exists");
        }

        return user;
    }
}
