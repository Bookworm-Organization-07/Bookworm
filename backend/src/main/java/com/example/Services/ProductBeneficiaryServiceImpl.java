package com.example.Services;

import com.example.Repository.BeneficiaryAssignmentRepository;
import com.example.Repository.BeneficiaryRepository;
import com.example.Repository.ProductBeneficiaryRepository;
import com.example.dto.ProductBeneficiaryDTO;
import com.example.models.Beneficiary;
import com.example.models.BeneficiaryAssignment;
import com.example.models.ProductBeneficiary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductBeneficiaryServiceImpl implements ProductBeneficiaryService {

    private final ProductBeneficiaryRepository repository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryAssignmentRepository beneficiaryAssignmentRepository;

    public ProductBeneficiaryServiceImpl(ProductBeneficiaryRepository repository,
                                         BeneficiaryRepository beneficiaryRepository,
                                         BeneficiaryAssignmentRepository beneficiaryAssignmentRepository) {
        this.repository = repository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.beneficiaryAssignmentRepository = beneficiaryAssignmentRepository;
    }

    /**
     * Every beneficiary, not just the ones who have already earned
     * something - beneficiary_assignment (not product_beneficiary) is
     * the source of "which products", since it holds the current
     * assignment regardless of whether a sale/rental/lend has happened
     * yet. product_beneficiary is payout HISTORY only, so it is used
     * here just to total up what a beneficiary has actually received for
     * that product, defaulting to zero when nothing has been paid out.
     * A beneficiary with no assigned products at all still gets one row,
     * with a null product and zero received, so the page never drops
     * them just for not having sold anything yet.
     */
    @Override
    public List<ProductBeneficiaryDTO> getAllProductBeneficiaries() {
        List<ProductBeneficiaryDTO> result = new ArrayList<>();

        for (Beneficiary beneficiary : beneficiaryRepository.findAll()) {
            List<BeneficiaryAssignment> assignments =
                    beneficiaryAssignmentRepository.findByBeneficiary_BeneficiaryId(beneficiary.getBeneficiaryId());

            if (assignments.isEmpty()) {
                result.add(toDto(beneficiary, null, BigDecimal.ZERO));
                continue;
            }

            for (BeneficiaryAssignment assignment : assignments) {
                BigDecimal received = repository
                        .findByBeneficiary_BeneficiaryIdAndProduct_ProductId(
                                beneficiary.getBeneficiaryId(), assignment.getProduct().getProductId())
                        .stream()
                        .map(ProductBeneficiary::getRoyaltyReceived)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                result.add(toDto(beneficiary, assignment.getProduct().getProductName(), received));
            }
        }

        return result;
    }

    private ProductBeneficiaryDTO toDto(Beneficiary beneficiary, String productName, BigDecimal royaltyReceived) {
        ProductBeneficiaryDTO dto = new ProductBeneficiaryDTO();
        dto.setBeneficiaryName(beneficiary.getBeneficiaryName());
        dto.setProductName(productName);
        dto.setRoyaltyReceived(royaltyReceived);
        return dto;
    }
}
