package com.bookworm.dto.auth;

public record AuthResponse(String token, UserSummary user) {
}
