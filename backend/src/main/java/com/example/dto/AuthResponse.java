package com.example.dto;

/**
 * What the browser needs after a successful login: the bearer token, and
 * enough about the user to render the navigation bar without a second
 * round trip.
 */
public class AuthResponse {

    private final String token;
    private final Integer userId;
    private final String userName;
    private final boolean admin;

    public AuthResponse(String token, Integer userId, String userName, boolean admin) {
        this.token = token;
        this.userId = userId;
        this.userName = userName;
        this.admin = admin;
    }

    public String getToken() {
        return token;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAdmin() {
        return admin;
    }
}
