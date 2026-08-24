package com.example.Controller;

import com.example.Services.LibraryCheckoutService;
import com.example.dto.LibraryCheckoutRequest;
import com.example.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/library")
public class LibraryCheckoutController {

    private final LibraryCheckoutService libraryCheckoutService;
    private final CurrentUser currentUser;

    public LibraryCheckoutController(LibraryCheckoutService libraryCheckoutService,
                                     CurrentUser currentUser) {
        this.libraryCheckoutService = libraryCheckoutService;
        this.currentUser = currentUser;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkoutLibrary(
            @RequestBody LibraryCheckoutRequest request) {
        libraryCheckoutService.checkout(currentUser.require(), request);
        return ResponseEntity.ok(Map.of("message", "Added to your library."));
    }
}
