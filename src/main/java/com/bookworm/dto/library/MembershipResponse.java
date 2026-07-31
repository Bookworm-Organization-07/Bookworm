package com.bookworm.dto.library;

import com.bookworm.entity.UserLibraryMembership;

import java.time.LocalDate;

public record MembershipResponse(
        Integer membershipId,
        String packageName,
        Integer booksUsed,
        Integer booksAllowed,
        LocalDate startDate,
        LocalDate expiryDate,
        String status) {

    public static MembershipResponse from(UserLibraryMembership m) {
        return new MembershipResponse(
                m.getMembershipId(),
                m.getLibraryPackage().getPackageName(),
                m.getBooksUsed(),
                m.getLibraryPackage().getBooksAllowed(),
                m.getStartDate(),
                m.getExpiryDate(),
                m.getStatus().name());
    }
}
