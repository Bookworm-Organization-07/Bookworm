package com.example.Services;

import com.example.Repository.CartRepository;
import com.example.models.Cart;
import com.example.models.Product;
import com.example.models.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart addToCart(User user, Product product, Integer qty, Integer rentDays) {
        int quantity = (qty == null || qty < 1) ? 1 : qty;

        if (rentDays != null) {
            if (rentDays < 1) {
                throw new IllegalArgumentException("Rental length must be at least one day.");
            }
            if (!product.isRentable()) {
                throw new IllegalStateException(product.getProductName() + " is not available to rent.");
            }
        }

        Optional<Cart> existing = cartRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            // A product only ever has one row in this reader's cart, so
            // re-adding it (e.g. clicking Rent on a book already sitting
            // in the cart as a purchase) updates that same row's choice
            // instead of creating a second one - this line's buy/rent
            // status is whatever the most recent add said.
            Cart cart = existing.get();
            cart.setQty(cart.getQty() + quantity);
            cart.setRentDays(rentDays);
            return cartRepository.save(cart);
        }

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQty(quantity);
        cart.setRentDays(rentDays);
        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    @Override
    public Cart updateQty(User user, Integer cartId, Integer qty) {
        if (qty == null || qty < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        Cart cart = ownedCartItem(user, cartId);
        cart.setQty(qty);
        return cartRepository.save(cart);
    }

    @Override
    public void removeItem(User user, Integer cartId) {
        cartRepository.delete(ownedCartItem(user, cartId));
    }

    @Override
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }

    /**
     * A cart id on its own says nothing about who owns it, so every
     * mutation confirms the row belongs to the caller first.
     */
    private Cart ownedCartItem(User user, Integer cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        if (cart.getUser().getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Cart item not found.");
        }
        return cart;
    }
}
