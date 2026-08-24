package com.example.Services;

import com.example.Repository.TransactionItemRepository;
import com.example.dto.OrderHistoryDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderHistoryService {

    private final TransactionItemRepository transactionItemRepository;

    public OrderHistoryService(TransactionItemRepository transactionItemRepository) {
        this.transactionItemRepository = transactionItemRepository;
    }

    public List<OrderHistoryDTO> getOrderHistory(int userId) {
        return transactionItemRepository.findOrderHistoryByUserId(userId);
    }

    /** Admin view: every reader's orders. */
    public List<OrderHistoryDTO> getAllOrderHistory() {
        return transactionItemRepository.findAllOrderHistory();
    }
}
