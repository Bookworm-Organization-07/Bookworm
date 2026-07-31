package com.bookworm.dto.cart;

import com.bookworm.service.InvoiceCalculator.BillBreakdown;

import java.util.List;

public record CartResponse(Integer cartId, List<CartItemResponse> items, BillBreakdown bill) {
}
