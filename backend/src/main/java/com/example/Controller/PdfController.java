package com.example.Controller;

import com.example.Repository.TransactionItemRepository;
import com.example.Repository.TransactionRepository;
import com.example.Services.TransactionPdfService;
import com.example.models.Transaction;
import com.example.models.TransactionItem;
import com.example.models.TransactionStatus;
import com.example.security.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoice")
public class PdfController {

    private final TransactionRepository transactionRepo;
    private final TransactionItemRepository itemRepo;
    private final TransactionPdfService pdfService;
    private final CurrentUser currentUser;

    public PdfController(TransactionRepository transactionRepo,
                         TransactionItemRepository itemRepo,
                         TransactionPdfService pdfService,
                         CurrentUser currentUser) {
        this.transactionRepo = transactionRepo;
        this.itemRepo = itemRepo;
        this.pdfService = pdfService;
        this.currentUser = currentUser;
    }

    /** A reader can download their own invoices and nobody else's. */
    @GetMapping("/{transactionId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long transactionId) {

        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (transaction.getUser().getUserId() != currentUser.require().getUserId()) {
            throw new IllegalArgumentException("Transaction not found.");
        }

        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException(
                    "An invoice is only available once the payment has gone through.");
        }

        List<TransactionItem> items = itemRepo.findByTransaction(transaction);
        byte[] pdf = pdfService.generateInvoice(transaction, items);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=invoice_" + transactionId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
