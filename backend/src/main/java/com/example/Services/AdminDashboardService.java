package com.example.Services;

import com.example.Repository.TransactionRepository;
import com.example.dto.AdminDashboardDto;
import com.example.models.Transaction;
import com.example.models.TransactionItem;
import com.example.models.TransactionStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The numbers behind the admin dashboard. Everything here is read from
 * transactions that already succeeded - nothing is written, this class
 * only totals up what CheckoutService and LibraryCheckoutServiceImpl
 * already recorded.
 */
@Service
public class AdminDashboardService {

    private final TransactionRepository transactionRepository;

    public AdminDashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public AdminDashboardDto getDashboard() {
        // The catalogue is small enough (an admin's own bookstore, not a
        // marketplace) that reading every successful transaction into
        // memory and totalling it up in plain Java is simpler to follow
        // than writing the equivalent as a handful of JPQL aggregate
        // queries - and just as fast at this scale.
        List<Transaction> successfulTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .toList();

        BigDecimal totalRevenue = successfulTransactions.stream()
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Every line from every successful transaction - a purchase, a
        // rental, or a library lend all count as a book reaching a
        // reader, so none of them is left out of the dashboard totals.
        List<TransactionItem> allItems = successfulTransactions.stream()
                .flatMap(t -> t.getItems().stream())
                .toList();

        long booksBought = allItems.stream()
                .mapToLong(TransactionItem::getQuantity)
                .sum();

        // Group the same items by product, add up how many of each were
        // bought/rented/lent, and keep whichever product has the highest
        // total - that is the bestseller.
        Map<String, Long> soldByProductName = allItems.stream()
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
