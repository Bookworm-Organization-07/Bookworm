package com.example.Services;

import com.example.Repository.TransactionRepository;
import com.example.dto.AdminDashboardDto;
import com.example.models.Transaction;
import com.example.models.TransactionItem;
import com.example.models.TransactionStatus;
import com.example.models.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class AdminDashboardService {

    private final TransactionRepository transactionRepository;

    public AdminDashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public AdminDashboardDto getDashboard() {

        List<Transaction> successfulTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .toList();

        BigDecimal totalRevenue = successfulTransactions.stream()
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransactionItem> boughtItems = successfulTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.BUY)
                .flatMap(t -> t.getItems().stream())
                .toList();

        long booksBought = boughtItems.stream()
                .mapToLong(TransactionItem::getQuantity)
                .sum();

        Map<String, Long> soldByProductName = boughtItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getProductName(),
                        Collectors.summingLong(TransactionItem::getQuantity)));

        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalRevenue(totalRevenue);
        dto.setBooksBought(booksBought);

        soldByProductName.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .ifPresent(best -> {
                    dto.setBestsellerProductName(best.getKey());
                    dto.setBestsellerCount(best.getValue());
                });

        return dto;
    }
}
