package com.bookworm.dto.library;

import com.bookworm.entity.LibraryPackage;

import java.math.BigDecimal;

public record LibraryPackageResponse(
        Integer libraryPackageId,
        String packageName,
        BigDecimal price,
        Integer validDays,
        Integer booksAllowed) {

    public static LibraryPackageResponse from(LibraryPackage pkg) {
        return new LibraryPackageResponse(
                pkg.getLibraryPackageId(), pkg.getPackageName(), pkg.getPrice(), pkg.getValidDays(), pkg.getBooksAllowed());
    }
}
