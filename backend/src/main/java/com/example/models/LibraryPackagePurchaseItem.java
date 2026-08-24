package com.example.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** One book borrowed against a LibraryPackagePurchase. */
@Entity
@Table(name = "library_package_purchase_item")
public class LibraryPackagePurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private LibraryPackagePurchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "royalty_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal royaltyPercent;

    @Column(name = "royalty_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal royaltyAmount;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public LibraryPackagePurchase getPurchase() {
        return purchase;
    }

    public void setPurchase(LibraryPackagePurchase purchase) {
        this.purchase = purchase;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getRoyaltyPercent() {
        return royaltyPercent;
    }

    public void setRoyaltyPercent(BigDecimal royaltyPercent) {
        this.royaltyPercent = royaltyPercent;
    }

    public BigDecimal getRoyaltyAmount() {
        return royaltyAmount;
    }

    public void setRoyaltyAmount(BigDecimal royaltyAmount) {
        this.royaltyAmount = royaltyAmount;
    }
}
