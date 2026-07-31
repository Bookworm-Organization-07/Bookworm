package com.bookworm.dto.order;

public record PaymentResultResponse(boolean success, String message, OrderResponse order) {
}
