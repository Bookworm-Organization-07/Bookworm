package com.example.dto;

import java.math.BigDecimal;

public class UpdateRoyaltyRequest {

    private BigDecimal royaltyPercent;

    public BigDecimal getRoyaltyPercent() {
        return royaltyPercent;
    }

    public void setRoyaltyPercent(BigDecimal royaltyPercent) {
        this.royaltyPercent = royaltyPercent;
    }
}
