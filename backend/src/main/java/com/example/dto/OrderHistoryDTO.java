package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Built directly by a JPQL constructor expression in
 * TransactionItemRepository, so the constructor's parameter order has to
 * stay in step with the SELECT clause there.
 */
public class OrderHistoryDTO {

    private Long transactionId;
    private String productName;
    private BigDecimal amount;
    private LocalDateTime orderDate;

    public OrderHistoryDTO(Long transactionId,
                           String productName,
                           BigDecimal amount,
                           LocalDateTime orderDate) {
        this.transactionId = transactionId;
        this.productName = productName;
        this.amount = amount;
        this.orderDate = orderDate;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
