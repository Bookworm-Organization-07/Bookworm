package com.example.Repository;

import com.example.models.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Integer> {

    /** Backs the "default to the Author" fallback - reuses one Author beneficiary across all of their books. */
    Optional<Beneficiary> findByBeneficiaryTypeAndBeneficiaryNameIgnoreCase(String beneficiaryType, String beneficiaryName);
}
