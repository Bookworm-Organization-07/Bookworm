package com.bookworm.entity;

import com.bookworm.entity.enums.PayoutStatus;
import com.bookworm.entity.enums.RoyaltySourceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "royalty_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "royalty_transaction_id")
    private Integer royaltyTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_stakeholder_id", nullable = false)
    private ProductStakeholder productStakeholder;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private RoyaltySourceType sourceType;

    /** Polymorphic pointer to ORDER_ITEMS.order_item_id or LIBRARY.library_id, per source_type. App-enforced, not a DB FK. */
    @Column(name = "source_reference_id", nullable = false)
    private Integer sourceReferenceId;

    @Column(name = "royalty_base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal royaltyBaseAmount;

    @Column(name = "royalty_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal royaltyAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false)
    private PayoutStatus payoutStatus;

    @Column(name = "payout_date")
    private LocalDateTime payoutDate;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    void prePersist() {
        if (transactionDate == null) transactionDate = LocalDateTime.now();
    }
}
