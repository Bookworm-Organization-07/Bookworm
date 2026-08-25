using BookwormApi.Models;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace BookwormApi.Service;

public class LibraryInvoicePdfService : ILibraryInvoicePdfService
{
    public byte[] GenerateLibraryInvoice(LibraryPackagePurchase purchase, List<LibraryPackagePurchaseItem> items)
    {
        var document = Document.Create(container =>
        {
            container.Page(page =>
            {
                page.Size(PageSizes.A4);
                page.Margin(30);
                page.DefaultTextStyle(x => x.FontSize(11));

                page.Header().Column(col =>
                {
                    col.Item().Text("Bookworm - Library Package Invoice").FontSize(18).Bold();
                    col.Item().Text($"Invoice ID: LIB-{purchase.PurchaseId}");
                    col.Item().Text($"Transaction ID: {purchase.TransactionId}");
                    col.Item().Text($"User: {purchase.User?.UserName}");
                    col.Item().Text($"Purchase Date: {purchase.PurchaseDate}");
                    col.Item().Text($"Package: {purchase.LibraryPackage?.Name}");
                    col.Item().Text($"Validity: {purchase.LibraryPackage?.ValidityDays} days");
                    col.Item().Text($"Book Limit: {purchase.AllowedBooks}");
                });

                page.Content().PaddingTop(15).Table(table =>
                {
                    table.ColumnsDefinition(columns =>
                    {
                        columns.RelativeColumn(3);
                        columns.RelativeColumn(1);
                        columns.RelativeColumn(1);
                    });

                    table.Header(header =>
                    {
                        header.Cell().Text("Book").Bold();
                        header.Cell().Text("Avg Price").Bold();
                        header.Cell().Text("Royalty").Bold();
                    });

                    foreach (var item in items)
                    {
                        table.Cell().Text(InvoiceNameHelper.InvoiceName(item.Product));
                        table.Cell().Text($"Rs {purchase.AvgBookPrice}");
                        table.Cell().Text($"Rs {item.RoyaltyAmount}");
                    }
                });

                page.Footer().PaddingTop(15).Text($"Total Paid: Rs {purchase.PackagePrice}").Bold();
            });
        });

        return document.GeneratePdf();
    }
}
