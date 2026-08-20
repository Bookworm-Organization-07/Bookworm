package com.example.Services;

import com.example.Repository.UserRepository;
import com.example.dto.AuthResponse;
import com.example.dto.GoogleTokenInfo;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.models.User;
import com.example.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AuthService {

    /**
     * Google's own endpoint for "is this ID token genuine, and whose is
     * it?" - see loginWithGoogle. Verifying against Google like this
     * (rather than checking the token's signature ourselves) keeps the
     * whole feature to one HTTP call and a few field checks, at the cost
     * of one extra network round trip per Google sign-in - a trade this
     * app's scale has no trouble affording.
     */
    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String googleClientId;

    public AuthService(UserRepository userRepo,
                       BCryptPasswordEncoder encoder,
                       JwtUtil jwtUtil,
                       @Value("${google.oauth.client-id}") String googleClientId) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.googleClientId = googleClientId;
    }

    /** The frontend's Google Sign-In button needs this to know which Google app it is signing in for. Not a secret - see SecurityConfig, it is a public GET. */
    public String getGoogleClientId() {
        return googleClientId;
    }

    public void register(RegisterRequest request) {
        if (userRepo.existsByUserEmail(request.getEmail())) {
            throw new IllegalStateException("That email is already registered.");
        }

        User user = new User();
        user.setUserName(request.getName());
        user.setUserEmail(request.getEmail());
        user.setUserPhone(request.getPhone());
        user.setUserAddress(request.getAddress());
        user.setUserPassword(encoder.encode(request.getPassword()));
        user.setJoinDate(LocalDate.now());
        // Never from the request body - see RegisterRequest.
        user.setAdmin(false);

        userRepo.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findByUserEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!encoder.matches(request.getPassword(), user.getUserPassword())) {
            // Same message as above, so the response cannot be used to
            // work out which email addresses exist.
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getUserEmail(), user.isAdmin());

        return new AuthResponse(token, user.getUserId(), user.getUserName(), user.isAdmin());
    }

    /**
     * "Sign in with Google" and "Sign up with Google" are the same
     * button and the same call: if this is the first time we have seen
     * this email, a new (never-admin) account is created for it on the
     * spot, exactly like a normal registration would - then either way,
     * the reader gets back the exact same kind of JWT a password login
     * would give them. Nothing downstream of this method - JwtAuthFilter,
     * CurrentUser, SecurityConfig - needs to know or care that a login
     * came from Google instead of a password.
     */
    public AuthResponse loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("No Google credential was provided.");
        }

        GoogleTokenInfo tokenInfo;
        try {
            tokenInfo = restTemplate.getForObject(GOOGLE_TOKENINFO_URL + idToken, GoogleTokenInfo.class);
        } catch (RestClientException ex) {
            // Google's tokeninfo endpoint answers a fake, expired, or
            // already-used token with its own 400 error body - which is
            // still a REST call failing, not a bug in this app. Without
            // this catch, that exception would otherwise reach
            // GlobalExceptionHandler as an unrecognised error and come
            // back to the browser as a raw 500.
            throw new IllegalArgumentException("Could not verify this Google sign-in.");
        }

        if (tokenInfo == null || tokenInfo.getEmail() == null) {
            throw new IllegalArgumentException("Could not verify this Google sign-in.");
        }

        // aud (audience) is which app a Google ID token was issued for.
        // Without this check, a token proving "this person signed into
        // SOME Google app" would be accepted as proof they signed into
        // OURS - which anyone could produce for any Google app of their
        // own, not just ours.
        if (!googleClientId.equals(tokenInfo.getAud())) {
            throw new IllegalArgumentException("This Google sign-in was not issued for this app.");
        }

        if (!tokenInfo.isEmailVerified()) {
            throw new IllegalArgumentException("This Google account's email address is not verified.");
        }

        User user = userRepo.findByUserEmail(tokenInfo.getEmail())
                .orElseGet(() -> registerFromGoogle(tokenInfo));

        String token = jwtUtil.generateToken(user.getUserEmail(), user.isAdmin());
        return new AuthResponse(token, user.getUserId(), user.getUserName(), user.isAdmin());
    }

    /**
     * A brand new account for a first-time Google sign-in. The password
     * column is NOT NULL and must stay usable by the regular
     * email+password login too (nothing stops this reader from also
     * signing in the normal way later) - so instead of leaving it
     * blank, it is set to a bcrypt hash of a random, never-shown UUID.
     * That hash cannot ever be guessed or typed by accident, so in
     * practice this account can only be signed into via Google, unless
     * the reader deliberately resets their password some other way.
     */
    private User registerFromGoogle(GoogleTokenInfo tokenInfo) {
        User user = new User();
        user.setUserName(tokenInfo.getName() != null ? tokenInfo.getName() : tokenInfo.getEmail());
        user.setUserEmail(tokenInfo.getEmail());
        user.setUserPassword(encoder.encode(UUID.randomUUID().toString()));
        user.setJoinDate(LocalDate.now());
        user.setAdmin(false);
        return userRepo.save(user);
    }
}
