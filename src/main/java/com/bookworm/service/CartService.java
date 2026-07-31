package com.bookworm.service;

import com.bookworm.dto.cart.CartItemResponse;
import com.bookworm.dto.cart.CartResponse;
import com.bookworm.entity.Cart;
import com.bookworm.entity.CartItem;
import com.bookworm.entity.LibraryPackage;
import com.bookworm.entity.Product;
import com.bookworm.entity.User;
import com.bookworm.entity.enums.AccessType;
import com.bookworm.entity.enums.ItemType;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.CartItemRepository;
import com.bookworm.repository.CartRepository;
import com.bookworm.repository.LibraryPackageRepository;
import com.bookworm.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final LibraryPackageRepository libraryPackageRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            LibraryPackageRepository libraryPackageRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.libraryPackageRepository = libraryPackageRepository;
    }

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Cart cart = getOrCreateCart(user);
        return toCartResponse(cart);
    }

    public CartResponse addProduct(User user, Integer productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ResourceNotFoundException("Product not available: " + productId);
        }

        boolean alreadyInCart = cartItemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .isPresent();
        if (!alreadyInCart) {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .itemType(ItemType.PRODUCT)
                    .product(product)
                    .accessType(AccessType.BUY)
                    .unitPrice(product.getSalePrice())
                    .quantity(1)
                    .build();
            cartItemRepository.save(item);
        }

        return toCartResponse(cart);
    }

    public CartResponse rentProduct(User user, Integer productId, Integer rentDays) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!Boolean.TRUE.equals(product.getIsActive()) || !Boolean.TRUE.equals(product.getIsRentable())) {
            throw new ResourceNotFoundException("Product not available for rent: " + productId);
        }
        if (product.getRentPricePerDay() == null) {
            throw new IllegalStateException("This title doesn't have a rental rate set yet.");
        }

        boolean alreadyInCart = cartItemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .isPresent();
        if (!alreadyInCart) {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .itemType(ItemType.PRODUCT)
                    .product(product)
                    .accessType(AccessType.RENT)
                    .rentDays(rentDays)
                    .unitPrice(product.getRentPricePerDay().multiply(BigDecimal.valueOf(rentDays)))
                    .quantity(1)
                    .build();
            cartItemRepository.save(item);
        }

        return toCartResponse(cart);
    }

    public CartResponse addLibraryPackage(User user, Integer libraryPackageId) {
        Cart cart = getOrCreateCart(user);
        LibraryPackage libraryPackage = libraryPackageRepository.findById(libraryPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("Library package not found: " + libraryPackageId));
        if (!Boolean.TRUE.equals(libraryPackage.getIsActive())) {
            throw new ResourceNotFoundException("Library package not available: " + libraryPackageId);
        }

        boolean alreadyInCart = cartItemRepository
                .findByCart_CartIdAndLibraryPackage_LibraryPackageId(cart.getCartId(), libraryPackageId)
                .isPresent();
        if (!alreadyInCart) {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .itemType(ItemType.LIBRARY_PACKAGE)
                    .libraryPackage(libraryPackage)
                    .accessType(AccessType.SUBSCRIBE)
                    .unitPrice(libraryPackage.getPrice())
                    .quantity(1)
                    .build();
            cartItemRepository.save(item);
        }

        return toCartResponse(cart);
    }

    public CartResponse removeItem(User user, Integer cartItemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .filter(i -> i.getCart().getCartId().equals(cart.getCartId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cartItemRepository.delete(item);
        return toCartResponse(cart);
    }

    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getCartId(),
                items.stream().map(CartItemResponse::from).toList(),
                InvoiceCalculator.compute(subtotal));
    }
}
