package com.bookworm.dto.cart;

import com.bookworm.entity.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Integer cartItemId,
        String itemType,
        String accessType,
        Integer productId,
        String title,
        String titleEnglish,
        String coverImageUrl,
        Integer libraryPackageId,
        String packageName,
        Integer rentDays,
        BigDecimal unitPrice,
        Integer quantity) {

    public static CartItemResponse from(CartItem item) {
        boolean isProduct = item.getProduct() != null;
        return new CartItemResponse(
                item.getCartItemId(),
                item.getItemType().name(),
                item.getAccessType().name(),
                isProduct ? item.getProduct().getProductId() : null,
                isProduct ? item.getProduct().getTitle() : null,
                isProduct ? item.getProduct().getTitleEnglish() : null,
                isProduct ? item.getProduct().getCoverImageUrl() : null,
                !isProduct ? item.getLibraryPackage().getLibraryPackageId() : null,
                !isProduct ? item.getLibraryPackage().getPackageName() : null,
                item.getRentDays(),
                item.getUnitPrice(),
                item.getQuantity());
    }
}
