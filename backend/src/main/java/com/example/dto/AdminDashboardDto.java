package com.example.dto;

import java.math.BigDecimal;

/** The handful of numbers an admin wants to see first, on the admin home page. */
public class AdminDashboardDto {

    private BigDecimal totalRevenue;
    private long booksBought;
    private String bestsellerProductName;
    private long bestsellerCount;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getBooksBought() {
        return booksBought;
    }

    public void setBooksBought(long booksBought) {
        this.booksBought = booksBought;
    }

    public String getBestsellerProductName() {
        return bestsellerProductName;
    }

    public void setBestsellerProductName(String bestsellerProductName) {
        this.bestsellerProductName = bestsellerProductName;
    }

    public long getBestsellerCount() {
        return bestsellerCount;
    }

    public void setBestsellerCount(long bestsellerCount) {
        this.bestsellerCount = bestsellerCount;
    }
}
