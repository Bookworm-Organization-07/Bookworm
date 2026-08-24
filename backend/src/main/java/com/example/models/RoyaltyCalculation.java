package com.example.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row per transaction item that earned royalty. total_royalty is the
 * pot; product_beneficiary then records how that pot was split.
 */
@Entity
@Table(name = "royalty_calculation")
public class RoyaltyCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roycal_id")
    private int roycalId;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private TransactionItem transactionItem;

    @Column(name = "roycal_trandate")
    private LocalDate roycalTranDate;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The line total the royalty percentage was applied to. */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "royalty_percent", precision = 5, scale = 2)
    private BigDecimal royaltyPercent;

    @Column(name = "total_royalty", precision = 12, scale = 2)
    private BigDecimal totalRoyalty;

    public RoyaltyCalculation() {
    }

    public int getRoycalId() {
        return roycalId;
    }

    public void setRoycalId(int roycalId) {
        this.roycalId = roycalId;
    }

    public TransactionItem getTransactionItem() {
        return transactionItem;
    }

    public void setTransactionItem(TransactionItem transactionItem) {
        this.transactionItem = transactionItem;
    }

    public LocalDate getRoycalTranDate() {
        return roycalTranDate;
    }

    public void setRoycalTranDate(LocalDate roycalTranDate) {
        this.roycalTranDate = roycalTranDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
