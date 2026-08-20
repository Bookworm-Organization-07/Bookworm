package com.example.Controller;

import com.example.Services.CheckoutService;
import com.example.models.Transaction;
import com.example.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CurrentUser currentUser;

    public CheckoutController(CheckoutService checkoutService, CurrentUser currentUser) {
        this.checkoutService = checkoutService;
        this.currentUser = currentUser;
    }

    /**
     * Each cart line already carries its own buy-or-rent choice (see
     * Cart.rentDays), so checkout takes no request body - it just
     * settles whatever is currently in the cart. That can produce one
     * or two transactions (a BUY one, a RENT one, or both if the cart
     * mixes the two), so the response is a list rather than a single
     * transaction summary.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> checkout() {
        List<Transaction> transactions = checkoutService.checkout(currentUser.require());

        List<Map<String, Object>> summaries = transactions.stream()
                .map(transaction -> Map.<String, Object>of(
                        "transactionId", transaction.getTransactionId(),
                        "totalAmount", transaction.getTotalAmount(),
                        "transactionType", transaction.getTransactionType().name()))
                .toList();

        return ResponseEntity.ok(Map.of("transactions", summaries));
    }
}
