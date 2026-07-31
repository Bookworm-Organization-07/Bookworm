package com.bookworm.dto.catalog;

import com.bookworm.entity.Language;

public record LanguageResponse(Integer languageId, String languageCode, String languageName, Boolean isDefault) {

    public static LanguageResponse from(Language language) {
        return new LanguageResponse(
                language.getLanguageId(),
                language.getLanguageCode(),
                language.getLanguageName(),
                language.getIsDefault());
    }
}
