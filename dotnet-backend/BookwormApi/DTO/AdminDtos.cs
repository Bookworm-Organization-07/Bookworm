namespace BookwormApi.DTO;

public class AdminDashboardDto
{
    public decimal TotalRevenue { get; set; }
    public long BooksBought { get; set; }
    public string? BestsellerProductName { get; set; }
    public long BestsellerCount { get; set; }
}

public class AdminUserSummaryDto
{
    public int UserId { get; set; }
    public string UserName { get; set; } = null!;
    public string UserEmail { get; set; } = null!;
    public bool Admin { get; set; }
    public DateOnly? JoinDate { get; set; }
}

public class BulkUploadResultDto
{
    public int TotalRows { get; set; }
    public int CreatedRows { get; set; }
    public int SkippedRows { get; set; }
    public int FailedRows { get; set; }
    public string Log { get; set; } = string.Empty;
}

public class ManualProductRequest
{
    public string? ProdName { get; set; }
    public string? NameEnglish { get; set; }
    public string? Type { get; set; }
    public string? Language { get; set; }
    public string? Genre { get; set; }
    public string? Author { get; set; }
    public string? Publisher { get; set; }
    public string? Description { get; set; }
    public string? ShortDescription { get; set; }
    public decimal? Price { get; set; }
    public decimal? SpecialPrice { get; set; }
    public bool Rentable { get; set; }
    public bool Library { get; set; }
}

public class QuickAddResultDto
{
    public string Message { get; set; } = null!;
    public int ProductId { get; set; }
}
