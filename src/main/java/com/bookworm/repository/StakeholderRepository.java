package com.bookworm.repository;

import com.bookworm.entity.Stakeholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StakeholderRepository extends JpaRepository<Stakeholder, Integer> {

    Optional<Stakeholder> findByFullNameIgnoreCase(String fullName);
}
