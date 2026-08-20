using BookwormApi.Models;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace BookwormApi.Service;

internal static class InvoiceNameHelper
{
    public static string InvoiceName(Product? product)
    {
        if (product is null) return "Unknown";
        return !string.IsNullOrWhiteSpace(product.ProductNameEnglish) ? product.ProductNameEnglish! : product.ProductName;
    }
}

public class TransactionPdfService : ITransactionPdfService
{
    public byte[] GenerateInvoice(Transaction transaction, List<TransactionItem> items)
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
                    col.Item().Text("Bookworm - Transaction Invoice").FontSize(18).Bold();
                    col.Item().Text($"Transaction ID: {transaction.TransactionId}");
                    col.Item().Text($"User: {transaction.User?.UserName}");
                    col.Item().Text($"Type: {transaction.TransactionType}");
                    col.Item().Text($"Status: {transaction.Status}");
                    col.Item().Text($"Date: {transaction.CreatedAt}");
                });

                page.Content().PaddingTop(15).Table(table =>
                {
                    table.ColumnsDefinition(columns =>
                    {
                        columns.RelativeColumn(3);
                        columns.RelativeColumn(1);
                        columns.RelativeColumn(1);
                        columns.RelativeColumn(1);
                    });

                    table.Header(header =>
                    {
                        header.Cell().Text("Book").Bold();
                        header.Cell().Text("Price").Bold();
                        header.Cell().Text("Qty").Bold();
                        header.Cell().Text("Total").Bold();
                    });

                    foreach (var item in items)
                    {
                        var price = item.Price ?? 0;
                        var qty = item.Quantity ?? 0;
                        table.Cell().Text(InvoiceNameHelper.InvoiceName(item.Product));
                        table.Cell().Text($"Rs {price}");
                        table.Cell().Text(qty.ToString());
                        table.Cell().Text($"Rs {price * qty}");
                    }
                });

                page.Footer().PaddingTop(15).Text($"Total Amount: Rs {transaction.TotalAmount}").Bold();
            });
        });

        return document.GeneratePdf();
    }
}
