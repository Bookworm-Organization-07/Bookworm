package com.bookworm.dto.order;

import com.bookworm.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderItemResponse(
        Integer orderItemId,
        String itemType,
        String accessType,
        Integer productId,
        String title,
        String titleEnglish,
        String coverImageUrl,
        Integer libraryPackageId,
        String packageName,
        Integer rentDays,
        LocalDate validFrom,
        LocalDate validTo,
        BigDecimal unitPrice,
        Integer quantity) {

    public static OrderItemResponse from(OrderItem item) {
        boolean isProduct = item.getProduct() != null;
        return new OrderItemResponse(
                item.getOrderItemId(),
                item.getItemType().name(),
                item.getAccessType().name(),
                isProduct ? item.getProduct().getProductId() : null,
                isProduct ? item.getProduct().getTitle() : null,
                isProduct ? item.getProduct().getTitleEnglish() : null,
                isProduct ? item.getProduct().getCoverImageUrl() : null,
                !isProduct ? item.getLibraryPackage().getLibraryPackageId() : null,
                !isProduct ? item.getLibraryPackage().getPackageName() : null,
                item.getRentDays(),
                item.getValidFrom(),
                item.getValidTo(),
                item.getUnitPrice(),
                item.getQuantity());
    }
}
