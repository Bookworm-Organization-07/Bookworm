package com.bookworm.repository;

import com.bookworm.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Integer> {

    Optional<AppConfig> findByConfigKey(String configKey);
}
