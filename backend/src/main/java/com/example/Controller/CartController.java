package com.example.Controller;

import com.example.Repository.ProductRepository;
import com.example.Services.CartService;
import com.example.dto.AddToCartRequest;
import com.example.dto.UpdateCartQtyRequest;
import com.example.models.Cart;
import com.example.models.Product;
import com.example.models.User;
import com.example.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The cart always belongs to whoever is holding the token. There is no
 * userId in any of these paths, so one reader cannot reach another's
 * cart by guessing an id.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    public CartController(CartService cartService,
                          ProductRepository productRepository,
                          CurrentUser currentUser) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<Cart> getCart() {
        return cartService.getCartByUser(currentUser.require());
    }

    @PostMapping("/add")
    public Cart addToCart(@RequestBody AddToCartRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("productId is required.");
        }

        User user = currentUser.require();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        return cartService.addToCart(user, product, request.getQty(), request.getRentDays());
    }

    @PutMapping("/{cartId}")
    public Cart updateQty(@PathVariable Integer cartId,
                          @RequestBody UpdateCartQtyRequest request) {
        return cartService.updateQty(currentUser.require(), cartId, request.getQty());
    }

    @DeleteMapping("/remove/{cartId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer cartId) {
        cartService.removeItem(currentUser.require(), cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(currentUser.require());
        return ResponseEntity.noContent().build();
    }
}
