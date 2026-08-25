using BookwormNotificationService.Models;
using BookwormNotificationService.Services;
using Microsoft.AspNetCore.Mvc;

namespace BookwormNotificationService.Controllers;

[ApiController]
[Route("api/notifications")]
public class NotificationsController : ControllerBase
{
    private readonly IEmailSenderService _emailSenderService;
    private readonly ILogger<NotificationsController> _logger;

    public NotificationsController(IEmailSenderService emailSenderService, ILogger<NotificationsController> logger)
    {
        _emailSenderService = emailSenderService;
        _logger = logger;
    }

    [HttpPost("invoice-email")]
    public async Task<IActionResult> SendInvoiceEmail([FromBody] InvoiceEmailRequest request)
    {
        _logger.LogInformation("Received invoice email request for transaction {TransactionId}", request.TransactionId);

        var sent = await _emailSenderService.SendInvoiceEmailAsync(request);
        if (!sent)
        {
            return StatusCode(StatusCodes.Status502BadGateway, new { message = "Failed to send invoice email." });
        }

        return Ok(new { message = "Invoice email handled." });
    }
}
