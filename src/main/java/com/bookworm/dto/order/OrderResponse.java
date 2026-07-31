package com.bookworm.dto.order;

import com.bookworm.entity.Order;
import com.bookworm.repository.OrderItemRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer orderId,
        String status,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal vatAmount,
        BigDecimal serviceChargeAmount,
        BigDecimal totalPayableAmount,
        String paymentMode,
        LocalDateTime createdAt,
        List<OrderItemResponse> items) {

    public static OrderResponse from(Order order, OrderItemRepository orderItemRepository) {
        List<OrderItemResponse> items = orderItemRepository.findByOrder_OrderId(order.getOrderId()).stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getOrderId(),
                order.getOrderStatus().name(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getVatAmount(),
                order.getServiceChargeAmount(),
                order.getTotalPayableAmount(),
                order.getPaymentMode() != null ? order.getPaymentMode().name() : null,
                order.getCreatedAt(),
                items);
    }
}
