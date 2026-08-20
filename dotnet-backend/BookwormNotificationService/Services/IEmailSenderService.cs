using BookwormNotificationService.Models;

namespace BookwormNotificationService.Services;

public interface IEmailSenderService
{
    Task<bool> SendInvoiceEmailAsync(InvoiceEmailRequest request);
}
