package com.example.security;

import com.example.Repository.UserRepository;
import com.example.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the signed-in user from the security context.
 *
 * Every endpoint that touches personal data (cart, shelf, library,
 * orders, checkout) goes through here rather than reading a userId out
 * of the URL or the request body. Otherwise anyone could pass someone
 * else's id and read or delete their data.
 */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Not signed in");
        }

        String email = authentication.getName();

        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalStateException("Signed-in user no longer exists"));
    }
}
