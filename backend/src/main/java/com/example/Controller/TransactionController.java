package com.example.Controller;

import com.example.Repository.TransactionRepository;
import com.example.Services.TransactionItemService;
import com.example.dto.TransactionItemDTO;
import com.example.models.Transaction;
import com.example.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepo;
    private final TransactionItemService transactionItemService;
    private final CurrentUser currentUser;

    public TransactionController(TransactionRepository transactionRepo,
                                 TransactionItemService transactionItemService,
                                 CurrentUser currentUser) {
        this.transactionRepo = transactionRepo;
        this.transactionItemService = transactionItemService;
        this.currentUser = currentUser;
    }

    @GetMapping("/mine")
    public List<Transaction> getMyTransactions() {
        return transactionRepo.findByUser_UserIdOrderByCreatedAtDesc(
                currentUser.require().getUserId());
    }

    @GetMapping("/{transactionId}/items")
    public List<TransactionItemDTO> getItems(@PathVariable Long transactionId) {
        requireOwnTransaction(transactionId);
        return transactionItemService.getItemsByTransactionId(transactionId);
    }

    private void requireOwnTransaction(Long transactionId) {
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (transaction.getUser().getUserId() != currentUser.require().getUserId()) {
            throw new IllegalArgumentException("Transaction not found.");
        }
    }
}
