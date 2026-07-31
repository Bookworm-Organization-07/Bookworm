package com.bookworm.service;

import com.bookworm.entity.Library;
import com.bookworm.entity.OrderItem;
import com.bookworm.entity.ProductStakeholder;
import com.bookworm.entity.RoyaltyTransaction;
import com.bookworm.entity.enums.PayoutStatus;
import com.bookworm.entity.enums.RoyaltySourceType;
import com.bookworm.repository.ProductStakeholderRepository;
import com.bookworm.repository.RoyaltyTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BRD §13.1 Revenue Distribution — three royalty formulas, one per acquisition path.
 * Every product can have several stakeholders (author, narrator, publisher, ...); each gets
 * their own ledger row at their configured royalty_percentage.
 */
@Service
@Transactional
public class RoyaltyService {

    private final ProductStakeholderRepository productStakeholderRepository;
    private final RoyaltyTransactionRepository royaltyTransactionRepository;

    public RoyaltyService(
            ProductStakeholderRepository productStakeholderRepository,
            RoyaltyTransactionRepository royaltyTransactionRepository) {
        this.productStakeholderRepository = productStakeholderRepository;
        this.royaltyTransactionRepository = royaltyTransactionRepository;
    }

    /** Purchase: royalty on the product's base_price, unaffected by any sale-time discount. */
    public void calculateForPurchase(OrderItem orderItem) {
        BigDecimal base = orderItem.getProduct().getBasePrice();
        recordForEachStakeholder(orderItem.getProduct().getProductId(), RoyaltySourceType.PURCHASE, orderItem.getOrderItemId(), base);
    }

    /** Rent: royalty on the total rent actually paid (daily rate x days). */
    public void calculateForRent(OrderItem orderItem) {
        BigDecimal base = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        recordForEachStakeholder(orderItem.getProduct().getProductId(), RoyaltySourceType.RENT, orderItem.getOrderItemId(), base);
    }

    /** Library lend: royalty on the package's average per-book cost (package price / books allowed). */
    public void calculateForLend(Library libraryEntry) {
        BigDecimal packagePrice = libraryEntry.getMembership().getLibraryPackage().getPrice();
        int booksAllowed = libraryEntry.getMembership().getLibraryPackage().getBooksAllowed();
        BigDecimal base = packagePrice.divide(BigDecimal.valueOf(booksAllowed), 2, RoundingMode.HALF_UP);
        recordForEachStakeholder(libraryEntry.getProduct().getProductId(), RoyaltySourceType.LIBRARY_LEND, libraryEntry.getLibraryId(), base);
    }

    private void recordForEachStakeholder(Integer productId, RoyaltySourceType sourceType, Integer sourceReferenceId, BigDecimal base) {
        for (ProductStakeholder ps : productStakeholderRepository.findByProduct_ProductId(productId)) {
            BigDecimal royalty = base
                    .multiply(ps.getRoyaltyPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            royaltyTransactionRepository.save(RoyaltyTransaction.builder()
                    .productStakeholder(ps)
                    .sourceType(sourceType)
                    .sourceReferenceId(sourceReferenceId)
                    .royaltyBaseAmount(base)
                    .royaltyAmount(royalty)
                    .payoutStatus(PayoutStatus.PENDING)
                    .build());
        }
    }
}
