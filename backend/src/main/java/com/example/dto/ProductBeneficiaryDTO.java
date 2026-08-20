package com.example.dto;

import java.math.BigDecimal;

public class ProductBeneficiaryDTO {

    private int prodbenId;
    private String beneficiaryName;
    private String productName;
    private BigDecimal royaltyReceived;

    public int getProdbenId() {
        return prodbenId;
    }

    public void setProdbenId(int prodbenId) {
        this.prodbenId = prodbenId;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getRoyaltyReceived() {
        return royaltyReceived;
    }

    public void setRoyaltyReceived(BigDecimal royaltyReceived) {
        this.royaltyReceived = royaltyReceived;
    }
}
