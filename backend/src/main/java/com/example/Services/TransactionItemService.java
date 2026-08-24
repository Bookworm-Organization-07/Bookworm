package com.example.Services;

import com.example.Repository.TransactionItemRepository;
import com.example.dto.TransactionItemDTO;
import com.example.models.TransactionItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionItemService {

    private final TransactionItemRepository transactionItemRepository;

    public TransactionItemService(TransactionItemRepository transactionItemRepository) {
        this.transactionItemRepository = transactionItemRepository;
    }

    public List<TransactionItemDTO> getAllItems() {
        return transactionItemRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<TransactionItemDTO> getItemsByTransactionId(Long transactionId) {
        return transactionItemRepository
                .findByTransaction_TransactionId(transactionId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private TransactionItemDTO mapToDTO(TransactionItem item) {
        TransactionItemDTO dto = new TransactionItemDTO();
        dto.setItemId(item.getItemId());
        dto.setTransactionId(item.getTransaction().getTransactionId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
