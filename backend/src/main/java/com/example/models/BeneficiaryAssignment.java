package com.example.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Which beneficiary currently receives royalty for which product - a
 * plain many-to-many join, separate from ProductBeneficiary (which is
 * payout HISTORY: one row per amount actually paid out at a past sale).
 * This table is current configuration only, and carries no money.
 */
@Entity
@Table(
    name = "beneficiary_assignment",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_benassign",
        columnNames = {"product_id", "beneficiary_id"}
    )
)
public class BeneficiaryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private int assignmentId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }
}
