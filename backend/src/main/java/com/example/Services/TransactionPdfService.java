package com.example.Services;

import com.example.models.Product;
import com.example.models.Transaction;
import com.example.models.TransactionItem;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Invoice for a buy or rent checkout. */
@Service
public class TransactionPdfService {

    public byte[] generateInvoice(Transaction transaction, List<TransactionItem> items) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Bookworm - Transaction Invoice", titleFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Transaction ID: " + transaction.getTransactionId(), normalFont));
        document.add(new Paragraph("User: " + transaction.getUser().getUserName(), normalFont));
        document.add(new Paragraph("Type: " + transaction.getTransactionType(), normalFont));
        document.add(new Paragraph("Status: " + transaction.getStatus(), normalFont));
        document.add(new Paragraph("Date: " + transaction.getCreatedAt(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell("Book");
        table.addCell("Price");
        table.addCell("Qty");
        table.addCell("Total");

        for (TransactionItem item : items) {
            BigDecimal lineTotal =
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            table.addCell(invoiceName(item.getProduct()));
            table.addCell("Rs " + item.getPrice());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("Rs " + lineTotal);
        }
        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Amount: Rs " + transaction.getTotalAmount(), titleFont));

        document.close();
        return out.toByteArray();
    }

    /**
     * OpenPDF's default font (Helvetica) only has Latin-script glyphs -
     * a title written in Marathi/Hindi/Konkani would print as a blank
     * cell, not an error, so this is easy to miss without knowing to
     * look for it. Falling back to the English name sidesteps that
     * entirely instead of needing to embed a Devanagari font.
     */
    private String invoiceName(Product product) {
        return product.getProductNameEnglish() != null && !product.getProductNameEnglish().isBlank()
                ? product.getProductNameEnglish()
                : product.getProductName();
    }
}
