using System.Net.Http.Json;
using BookwormApi.Models;

namespace BookwormApi.Service;

/// <summary>
/// No longer sends mail itself - delegates to the standalone BookwormNotificationService
/// over HTTP (mandatory requirement #9's pattern, applied to a second, purpose-built
/// microservice instead of the Java demo). Delivery failures are logged and swallowed so a
/// down/unreachable notification service never fails a checkout - it already behaved this
/// way when mail was simply unconfigured, this keeps that same guarantee.
/// </summary>
public class EmailService : IEmailService
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<EmailService> _logger;

    public EmailService(HttpClient httpClient, ILogger<EmailService> logger)
    {
        _httpClient = httpClient;
        _logger = logger;
    }

    public async Task SendTransactionSuccessEmailAsync(string toEmail, Transaction transaction, byte[] invoicePdf)
    {
        try
        {
            var request = new
            {
                ToEmail = toEmail,
                RecipientName = transaction.User?.UserName ?? "Customer",
                TransactionId = transaction.TransactionId,
                Amount = transaction.TotalAmount,
                PdfBase64 = Convert.ToBase64String(invoicePdf),
                PdfFileName = $"invoice_{transaction.TransactionId}.pdf"
            };

            var response = await _httpClient.PostAsJsonAsync("/api/notifications/invoice-email", request);
            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning(
                    "Notification service returned {StatusCode} for transaction {TransactionId} invoice email",
                    response.StatusCode, transaction.TransactionId);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex,
                "Could not reach the notification service to send the invoice email for transaction {TransactionId} - checkout still succeeds",
                transaction.TransactionId);
        }
    }
}
