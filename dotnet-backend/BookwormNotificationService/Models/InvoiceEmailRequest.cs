using System.ComponentModel.DataAnnotations;

namespace BookwormNotificationService.Models;

public class InvoiceEmailRequest
{
    [Required, EmailAddress]
    public string ToEmail { get; set; } = string.Empty;

    [Required]
    public string RecipientName { get; set; } = string.Empty;

    [Required]
    public long TransactionId { get; set; }

    [Required]
    public decimal Amount { get; set; }

    [Required]
    public string PdfBase64 { get; set; } = string.Empty;

    [Required]
    public string PdfFileName { get; set; } = string.Empty;
}
