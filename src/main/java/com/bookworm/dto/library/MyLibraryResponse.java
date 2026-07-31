package com.bookworm.dto.library;

import java.util.List;

public record MyLibraryResponse(List<MembershipResponse> memberships, List<LibraryEntryResponse> entries) {
}
