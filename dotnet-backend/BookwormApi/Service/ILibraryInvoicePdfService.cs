using BookwormApi.Models;

namespace BookwormApi.Service;

public interface ILibraryInvoicePdfService
{
    byte[] GenerateLibraryInvoice(LibraryPackagePurchase purchase, List<LibraryPackagePurchaseItem> items);
}
