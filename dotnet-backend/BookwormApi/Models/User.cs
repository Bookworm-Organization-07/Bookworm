namespace BookwormApi.Models;

public class User
{
    public int UserId { get; set; }
    public string UserName { get; set; } = null!;
    public string UserEmail { get; set; } = null!;
    public string? UserPhone { get; set; }
    public string? UserAddress { get; set; }
    public string UserPassword { get; set; } = null!;
    public bool Admin { get; set; }
    public DateOnly? JoinDate { get; set; }
}
