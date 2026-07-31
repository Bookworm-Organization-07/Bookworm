package com.bookworm.service;

import com.bookworm.entity.AppConfig;
import java.util.List;
import java.util.Optional;

public interface AppConfigService {
    List<AppConfig> getAllConfigs();
    Optional<AppConfig> getConfigByKey(String key);
    AppConfig saveConfig(AppConfig config);
}