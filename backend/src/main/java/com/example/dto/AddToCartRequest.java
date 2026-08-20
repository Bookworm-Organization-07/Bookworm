package com.example.dto;

public class AddToCartRequest {

    private Integer productId;
    private Integer qty;

    /** Absent/null means this line is a purchase; a value means a rental for that many days. */
    private Integer rentDays;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getRentDays() {
        return rentDays;
    }

    public void setRentDays(Integer rentDays) {
        this.rentDays = rentDays;
    }
}
