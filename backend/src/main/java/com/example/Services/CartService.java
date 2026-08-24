package com.example.Services;

import com.example.models.Cart;
import com.example.models.Product;
import com.example.models.User;

import java.util.List;

public interface CartService {

    Cart addToCart(User user, Product product, Integer qty, Integer rentDays);

    List<Cart> getCartByUser(User user);

    Cart updateQty(User user, Integer cartId, Integer qty);

    void removeItem(User user, Integer cartId);

    void clearCart(User user);
}
