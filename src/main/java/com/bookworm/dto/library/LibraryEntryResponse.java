package com.bookworm.dto.library;

import com.bookworm.entity.Library;

import java.time.LocalDateTime;

public record LibraryEntryResponse(
        Integer libraryId,
        Integer productId,
        String title,
        String titleEnglish,
        String coverImageUrl,
        String accessType,
        LocalDateTime startDate,
        LocalDateTime expiryDate,
        String status) {

    public static LibraryEntryResponse from(Library entry) {
        return new LibraryEntryResponse(
                entry.getLibraryId(),
                entry.getProduct().getProductId(),
                entry.getProduct().getTitle(),
                entry.getProduct().getTitleEnglish(),
                entry.getProduct().getCoverImageUrl(),
                entry.getAccessType().name(),
                entry.getStartDate(),
                entry.getExpiryDate(),
                entry.getStatus().name());
    }
}
