package com.bookworm.dto.shelf;

import com.bookworm.entity.Shelf;

import java.time.LocalDateTime;

public record ShelfItemResponse(
        Integer shelfId,
        Integer productId,
        String title,
        String titleEnglish,
        String coverImageUrl,
        LocalDateTime acquiredAt) {

    public static ShelfItemResponse from(Shelf shelf) {
        return new ShelfItemResponse(
                shelf.getShelfId(),
                shelf.getProduct().getProductId(),
                shelf.getProduct().getTitle(),
                shelf.getProduct().getTitleEnglish(),
                shelf.getProduct().getCoverImageUrl(),
                shelf.getAcquiredAt());
    }
}
