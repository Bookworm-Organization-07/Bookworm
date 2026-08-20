package com.example.Services;

import com.example.Repository.RoyaltyCalculationRepository;
import com.example.dto.RoyaltyLedgerEntryDto;
import com.example.models.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reads back the royalty_calculation table as a ledger: the full,
 * ordered history of every royalty calculation CheckoutService and
 * LibraryCheckoutServiceImpl have ever written (see those two classes
 * for where entries come from - this class only reads).
 */
@Service
public class RoyaltyLedgerService {

    private final RoyaltyCalculationRepository royaltyCalculationRepository;

    public RoyaltyLedgerService(RoyaltyCalculationRepository royaltyCalculationRepository) {
        this.royaltyCalculationRepository = royaltyCalculationRepository;
    }

    public List<RoyaltyLedgerEntryDto> getLedger() {
        return royaltyCalculationRepository.findAllByOrderByRoycalTranDateDescRoycalIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private RoyaltyLedgerEntryDto toDto(com.example.models.RoyaltyCalculation royalty) {
        Transaction transaction = royalty.getTransactionItem().getTransaction();

        RoyaltyLedgerEntryDto dto = new RoyaltyLedgerEntryDto();
        dto.setRoycalId(royalty.getRoycalId());
        dto.setTranDate(royalty.getRoycalTranDate());
        dto.setProductName(royalty.getProduct().getProductName());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTransactionType(transaction.getTransactionType().name());
        dto.setTotalAmount(royalty.getTotalAmount());
        dto.setRoyaltyPercent(royalty.getRoyaltyPercent());
        dto.setTotalRoyalty(royalty.getTotalRoyalty());
        return dto;
    }
}
