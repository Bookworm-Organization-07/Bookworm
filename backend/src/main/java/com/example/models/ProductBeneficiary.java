package com.example.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** One beneficiary's share of a single RoyaltyCalculation. */
@Entity
@Table(name = "product_beneficiary")
public class ProductBeneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prodben_id")
    private int prodbenId;

    @ManyToOne
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "roycal_id", nullable = false)
    private RoyaltyCalculation royaltyCalculation;

    @Column(name = "royalty_received", precision = 12, scale = 2)
    private BigDecimal royaltyReceived;

    public ProductBeneficiary() {
    }

    public int getProdbenId() {
        return prodbenId;
    }

    public void setProdbenId(int prodbenId) {
        this.prodbenId = prodbenId;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public RoyaltyCalculation getRoyaltyCalculation() {
        return royaltyCalculation;
    }

    public void setRoyaltyCalculation(RoyaltyCalculation royaltyCalculation) {
        this.royaltyCalculation = royaltyCalculation;
    }

    public BigDecimal getRoyaltyReceived() {
        return royaltyReceived;
    }

    public void setRoyaltyReceived(BigDecimal royaltyReceived) {
        this.royaltyReceived = royaltyReceived;
    }
}
