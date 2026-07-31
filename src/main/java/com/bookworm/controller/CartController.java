package com.bookworm.controller;

import com.bookworm.dto.cart.AddCartItemRequest;
import com.bookworm.dto.cart.AddLibraryPackageRequest;
import com.bookworm.dto.cart.CartResponse;
import com.bookworm.dto.cart.RentProductRequest;
import com.bookworm.entity.User;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(currentUser(authentication));
    }

    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request, Authentication authentication) {
        return cartService.addProduct(currentUser(authentication), request.getProductId());
    }

    @PostMapping("/items/rent")
    public CartResponse rentItem(@Valid @RequestBody RentProductRequest request, Authentication authentication) {
        return cartService.rentProduct(currentUser(authentication), request.getProductId(), request.getRentDays());
    }

    @PostMapping("/packages")
    public CartResponse addPackage(@Valid @RequestBody AddLibraryPackageRequest request, Authentication authentication) {
        return cartService.addLibraryPackage(currentUser(authentication), request.getLibraryPackageId());
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItem(@PathVariable Integer cartItemId, Authentication authentication) {
        return cartService.removeItem(currentUser(authentication), cartItemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
