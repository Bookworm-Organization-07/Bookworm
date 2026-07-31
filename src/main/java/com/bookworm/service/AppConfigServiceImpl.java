package com.bookworm.service;

import com.bookworm.entity.AppConfig;
import com.bookworm.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AppConfigServiceImpl implements AppConfigService {

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Override
    public List<AppConfig> getAllConfigs() {
        return appConfigRepository.findAll();
    }

    @Override
    public Optional<AppConfig> getConfigByKey(String key) {
        return appConfigRepository.findById(key);
    }

    @Override
    public AppConfig saveConfig(AppConfig config) {
        return appConfigRepository.save(config);
    }
}
