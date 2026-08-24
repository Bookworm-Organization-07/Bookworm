package com.example.Repository;

import com.example.models.Cart;
import com.example.models.Product;
import com.example.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUserAndProduct(User user, Product product);

    List<Cart> findByUser(User user);

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByProduct_ProductId(int productId);
}
