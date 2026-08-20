using BookwormApi.Models;

namespace BookwormApi.Service;

public interface IEmailService
{
    Task SendTransactionSuccessEmailAsync(string toEmail, Transaction transaction, byte[] invoicePdf);
}
