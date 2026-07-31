package com.bookworm.config;

import com.bookworm.repository.OrderRepository;
import com.bookworm.repository.ProductRepository;
import com.bookworm.repository.UserRepository;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

/** TODO #6 — Actuator extension: live catalog/user counts under /actuator/info. */
@Component
public class BookwormInfoContributor implements InfoContributor {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public BookwormInfoContributor(ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("bookworm", Map.of(
                "productCount", productRepository.count(),
                "registeredUsers", userRepository.count(),
                "ordersPlaced", orderRepository.count()));
    }
}
