using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/invoice")]
[Authorize]
public class PdfController : ControllerBase
{
    private readonly IInvoiceService _invoiceService;
    private readonly ICurrentUserService _currentUserService;

    public PdfController(IInvoiceService invoiceService, ICurrentUserService currentUserService)
    {
        _invoiceService = invoiceService;
        _currentUserService = currentUserService;
    }

    [HttpGet("{transactionId:long}")]
    public async Task<IActionResult> GetInvoice(long transactionId)
    {
        var user = await _currentUserService.RequireAsync();
        var pdf = await _invoiceService.GetInvoicePdfAsync(user, transactionId);
        Response.Headers.ContentDisposition = $"attachment; filename=invoice_{transactionId}.pdf";
        return File(pdf, "application/pdf");
    }
}
