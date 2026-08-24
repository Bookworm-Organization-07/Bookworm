package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Google's own answer to "is this ID token real, and whose is it?" -
 * see AuthService.loginWithGoogle, which calls
 * https://oauth2.googleapis.com/tokeninfo?id_token=... and gets this
 * shape back. Google's response has more fields than this (name,
 * picture, issued/expiry times, ...) - @JsonIgnoreProperties means
 * Jackson quietly ignores whatever this class does not ask for, instead
 * of failing to parse the response over a field nobody needs here.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenInfo {

    /** Which app this token was issued for - must match our own Google client id, or anyone else's Google login would work here too. */
    private String aud;

    private String email;

    /** Google sends this back as the literal string "true"/"false", not a JSON boolean - see isEmailVerified(). */
    @JsonProperty("email_verified")
    private String emailVerified;

    private String name;

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return "true".equals(emailVerified);
    }

    public void setEmailVerified(String emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
