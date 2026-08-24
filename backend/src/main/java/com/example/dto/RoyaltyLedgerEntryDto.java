package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of the royalty ledger: the full history of every royalty
 * calculated, in the order it happened. Where ProductBeneficiaryDTO
 * answers "how much has each beneficiary earned in total", this answers
 * "what calculation happened, on what transaction, and when" - the
 * audit trail behind those totals.
 */
public class RoyaltyLedgerEntryDto {

    private int roycalId;
    private LocalDate tranDate;
    private String productName;
    private Long transactionId;
    private String transactionType;
    private BigDecimal totalAmount;
    private BigDecimal royaltyPercent;
    private BigDecimal totalRoyalty;

    public int getRoycalId() {
        return roycalId;
    }

    public void setRoycalId(int roycalId) {
        this.roycalId = roycalId;
    }

    public LocalDate getTranDate() {
        return tranDate;
    }

    public void setTranDate(LocalDate tranDate) {
        this.tranDate = tranDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRoyaltyPercent() {
        return royaltyPercent;
    }

    public void setRoyaltyPercent(BigDecimal royaltyPercent) {
        this.royaltyPercent = royaltyPercent;
    }

    public BigDecimal getTotalRoyalty() {
        return totalRoyalty;
    }

    public void setTotalRoyalty(BigDecimal totalRoyalty) {
        this.totalRoyalty = totalRoyalty;
    }
}
