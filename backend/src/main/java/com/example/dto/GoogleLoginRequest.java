package com.example.dto;

/** What the frontend sends after Google Identity Services hands it a signed-in user. */
public class GoogleLoginRequest {

    private String idToken;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
