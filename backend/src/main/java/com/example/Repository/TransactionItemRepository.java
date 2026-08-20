package com.example.Repository;

import com.example.dto.OrderHistoryDTO;
import com.example.models.Transaction;
import com.example.models.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface TransactionItemRepository extends JpaRepository<TransactionItem, Integer> {

    List<TransactionItem> findByTransaction(Transaction transaction);

    List<TransactionItem> findByTransaction_TransactionId(Long transactionId);

    @Query("""
            SELECT new com.example.dto.OrderHistoryDTO(
                t.transactionId,
                p.productName,
                (ti.price * ti.quantity),
                t.createdAt
            )
            FROM TransactionItem ti
            JOIN ti.transaction t
            JOIN t.user u
            JOIN ti.product p
            WHERE u.userId = :userId
            ORDER BY t.createdAt DESC
            """)
    List<OrderHistoryDTO> findOrderHistoryByUserId(@Param("userId") int userId);

    @Query("""
            SELECT new com.example.dto.OrderHistoryDTO(
                t.transactionId,
                p.productName,
                (ti.price * ti.quantity),
                t.createdAt
            )
            FROM TransactionItem ti
            JOIN ti.transaction t
            JOIN ti.product p
            ORDER BY t.createdAt DESC
            """)
    List<OrderHistoryDTO> findAllOrderHistory();
}
