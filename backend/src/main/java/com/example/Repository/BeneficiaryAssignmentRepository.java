package com.example.Repository;

import com.example.models.BeneficiaryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BeneficiaryAssignmentRepository extends JpaRepository<BeneficiaryAssignment, Integer> {

    List<BeneficiaryAssignment> findByProduct_ProductId(int productId);

    List<BeneficiaryAssignment> findByBeneficiary_BeneficiaryId(int beneficiaryId);

    boolean existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(int productId, int beneficiaryId);

    @Transactional
    void deleteByProduct_ProductIdAndBeneficiary_BeneficiaryId(int productId, int beneficiaryId);

    @Transactional
    void deleteByProduct_ProductId(int productId);
}
