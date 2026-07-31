package com.bookworm.controller;

import com.bookworm.dto.order.OrderResponse;
import com.bookworm.dto.order.PaymentRequest;
import com.bookworm.dto.order.PaymentResultResponse;
import com.bookworm.entity.User;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    public PaymentResultResponse checkout(@Valid @RequestBody PaymentRequest request, Authentication authentication) {
        return orderService.checkout(currentUser(authentication), request);
    }

    @PostMapping("/{orderId}/retry-pay")
    public PaymentResultResponse retryPay(
            @PathVariable Integer orderId, @Valid @RequestBody PaymentRequest request, Authentication authentication) {
        return orderService.retryPay(currentUser(authentication), orderId, request);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable Integer orderId, Authentication authentication) {
        return orderService.cancel(currentUser(authentication), orderId);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Integer orderId, Authentication authentication) {
        return orderService.getOrder(currentUser(authentication), orderId);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
