package com.example.Services;

import com.example.Repository.BeneficiaryAssignmentRepository;
import com.example.Repository.BeneficiaryRepository;
import com.example.models.Author;
import com.example.models.Beneficiary;
import com.example.models.BeneficiaryAssignment;
import com.example.models.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Which beneficiaries a product's royalty is currently split across,
 * and the two admin actions on that list: assign an existing
 * beneficiary, or unassign one.
 *
 * A product is never left with zero beneficiaries. If nothing has been
 * explicitly assigned, the product's own Author stands in as the
 * default (see getAssignedBeneficiaries) - and once at least one
 * beneficiary is assigned, unassign refuses to remove the last one.
 */
@Service
public class BeneficiaryAssignmentService {

    private static final String AUTHOR_TYPE = "Author";

    private final BeneficiaryAssignmentRepository assignmentRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryAssignmentService(BeneficiaryAssignmentRepository assignmentRepository,
                                        BeneficiaryRepository beneficiaryRepository) {
        this.assignmentRepository = assignmentRepository;
        this.beneficiaryRepository = beneficiaryRepository;
    }

    /**
     * The beneficiaries a sale of this product should split royalty
     * across. If none have been explicitly assigned yet, the product's
     * Author is assigned as the default here (reusing the same Author
     * beneficiary across every other book by them, rather than creating
     * a new one per book) - so this never returns an empty list for a
     * product that has an author, and the admin screen and checkout
     * always agree on who is currently assigned.
     */
    @Transactional
    public List<Beneficiary> getAssignedBeneficiaries(Product product) {
        List<BeneficiaryAssignment> assignments = assignmentRepository.findByProduct_ProductId(product.getProductId());
        if (!assignments.isEmpty()) {
            return assignments.stream().map(BeneficiaryAssignment::getBeneficiary).toList();
        }

        Author author = product.getAuthor();
        if (author == null) {
            return List.of(); // nothing configured, and no author to fall back to
        }

        Beneficiary authorBeneficiary = beneficiaryRepository
                .findByBeneficiaryTypeAndBeneficiaryNameIgnoreCase(AUTHOR_TYPE, author.getName())
                .orElseGet(() -> {
                    Beneficiary created = new Beneficiary();
                    created.setBeneficiaryName(author.getName());
                    created.setBeneficiaryType(AUTHOR_TYPE);
                    return beneficiaryRepository.save(created);
                });

        assign(product, authorBeneficiary.getBeneficiaryId());
        return List.of(authorBeneficiary);
    }

    /** Attaching a beneficiary that is already assigned is a harmless no-op, not an error. */
    @Transactional
    public void assign(Product product, int beneficiaryId) {
        if (assignmentRepository.existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(product.getProductId(), beneficiaryId)) {
            return;
        }
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found: " + beneficiaryId));

        BeneficiaryAssignment assignment = new BeneficiaryAssignment();
        assignment.setProduct(product);
        assignment.setBeneficiary(beneficiary);
        assignmentRepository.save(assignment);
    }

    /**
     * Refuses to remove the last beneficiary a product has - a book's
     * royalty must always have somewhere to go, even if that means
     * falling back to the Author default the next time it is read.
     */
    @Transactional
    public void unassign(Product product, int beneficiaryId) {
        List<BeneficiaryAssignment> current = assignmentRepository.findByProduct_ProductId(product.getProductId());

        boolean isAssigned = current.stream()
                .anyMatch(a -> a.getBeneficiary().getBeneficiaryId() == beneficiaryId);
        if (!isAssigned) {
            throw new IllegalArgumentException("That beneficiary is not assigned to this book.");
        }
        if (current.size() <= 1) {
            throw new IllegalStateException(
                    "Can't remove the last beneficiary - every book needs at least one.");
        }

        assignmentRepository.deleteByProduct_ProductIdAndBeneficiary_BeneficiaryId(product.getProductId(), beneficiaryId);
    }
}
