package com.example.Controller;

import com.example.Services.OrderHistoryService;
import com.example.dto.OrderHistoryDTO;
import com.example.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;
    private final CurrentUser currentUser;

    public OrderHistoryController(OrderHistoryService orderHistoryService,
                                  CurrentUser currentUser) {
        this.orderHistoryService = orderHistoryService;
        this.currentUser = currentUser;
    }

    /** The signed-in reader's own orders. */
    @GetMapping("/history/mine")
    public List<OrderHistoryDTO> getMyOrderHistory() {
        return orderHistoryService.getOrderHistory(currentUser.require().getUserId());
    }

    /** Every reader's orders. Admin only - see SecurityConfig. */
    @GetMapping("/history")
    public List<OrderHistoryDTO> getAllOrders() {
        return orderHistoryService.getAllOrderHistory();
    }
}
