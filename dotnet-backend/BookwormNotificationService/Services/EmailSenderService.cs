using BookwormNotificationService.Models;
using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;

namespace BookwormNotificationService.Services;

public class EmailSenderService : IEmailSenderService
{
    private readonly MailOptions _options;
    private readonly ILogger<EmailSenderService> _logger;

    public EmailSenderService(IOptions<MailOptions> options, ILogger<EmailSenderService> logger)
    {
        _options = options.Value;
        _logger = logger;
    }

    public async Task<bool> SendInvoiceEmailAsync(InvoiceEmailRequest request)
    {
        if (string.IsNullOrWhiteSpace(_options.Username))
        {
            _logger.LogInformation("Mail is not configured - skipping invoice email for transaction {TransactionId}", request.TransactionId);
            return true;
        }

        try
        {
            var message = new MimeMessage();
            message.From.Add(MailboxAddress.Parse(_options.Username));
            message.To.Add(MailboxAddress.Parse(request.ToEmail));
            message.Subject = "Bookworm - Transaction Successful";

            var builder = new BodyBuilder
            {
                TextBody =
                    $"Hello {request.RecipientName},\n\n" +
                    "Your transaction was successful.\n\n" +
                    $"Transaction ID: {request.TransactionId}\n" +
                    $"Amount: Rs {request.Amount}\n\n" +
                    "Thank you for shopping with Bookworm!"
            };
            builder.Attachments.Add(request.PdfFileName, Convert.FromBase64String(request.PdfBase64), new ContentType("application", "pdf"));
            message.Body = builder.ToMessageBody();

            using var client = new SmtpClient();
            await client.ConnectAsync(_options.Host, _options.Port, SecureSocketOptions.StartTls);
            await client.AuthenticateAsync(_options.Username, _options.Password);
            await client.SendAsync(message);
            await client.DisconnectAsync(true);

            _logger.LogInformation("Sent invoice email for transaction {TransactionId} to {ToEmail}", request.TransactionId, request.ToEmail);
            return true;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Could not send the invoice email for transaction {TransactionId}", request.TransactionId);
            return false;
        }
    }
}
