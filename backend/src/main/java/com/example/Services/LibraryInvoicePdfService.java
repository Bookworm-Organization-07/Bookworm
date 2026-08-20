package com.example.Services;

import com.example.models.LibraryPackagePurchase;
import com.example.models.LibraryPackagePurchaseItem;
import com.example.models.Product;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/** Invoice for a library package purchase and the titles borrowed on it. */
@Service
public class LibraryInvoicePdfService {

    public byte[] generateLibraryInvoice(LibraryPackagePurchase purchase,
                                         List<LibraryPackagePurchaseItem> items) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Bookworm - Library Package Invoice", titleFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Invoice ID: LIB-" + purchase.getPurchaseId(), normalFont));
        document.add(new Paragraph(
                "Transaction ID: " + purchase.getTransaction().getTransactionId(), normalFont));
        document.add(new Paragraph("User: " + purchase.getUser().getUserName(), normalFont));
        document.add(new Paragraph("Purchase Date: " + purchase.getPurchaseDate(), normalFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Package Details", boldFont));
        document.add(new Paragraph(
                "Package Name: " + purchase.getLibraryPackage().getName(), normalFont));
        document.add(new Paragraph(
                "Validity: " + purchase.getLibraryPackage().getValidityDays() + " days", normalFont));
        document.add(new Paragraph("Book Limit: " + purchase.getAllowedBooks(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell("Book");
        table.addCell("Avg Price");
        table.addCell("Royalty");

        for (LibraryPackagePurchaseItem item : items) {
            table.addCell(invoiceName(item.getProduct()));
            table.addCell("Rs " + purchase.getAvgBookPrice());
            table.addCell("Rs " + item.getRoyaltyAmount());
        }
        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Paid: Rs " + purchase.getPackagePrice(), titleFont));

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
