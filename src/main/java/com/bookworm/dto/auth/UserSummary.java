package com.bookworm.dto.auth;

import com.bookworm.entity.Admin;
import com.bookworm.entity.User;

public record UserSummary(Integer userId, String fullName, String email, String userType) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getUserId(), user.getFullName(), user.getEmail(), user.getUserType().name());
    }

    public static UserSummary from(Admin admin) {
        return new UserSummary(admin.getAdminId(), admin.getFullName(), admin.getEmail(), admin.getRole().name());
    }
}
