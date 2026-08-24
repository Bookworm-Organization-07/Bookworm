package com.example.Repository;

import com.example.models.RoyaltyCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoyaltyCalculationRepository extends JpaRepository<RoyaltyCalculation, Integer> {

    /**
     * Newest first - a ledger reads most naturally with the latest
     * entry on top. Sorting by id too (not just the date) keeps entries
     * from the same day in a stable, predictable order.
     */
    List<RoyaltyCalculation> findAllByOrderByRoycalTranDateDescRoycalIdDesc();
}
