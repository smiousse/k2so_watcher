package com.k2so.watcher.service;

import com.k2so.watcher.model.AppSettings;
import com.k2so.watcher.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class AppSettingsService {

    private final AppSettingsRepository appSettingsRepository;

    public AppSettingsService(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    @PostConstruct
    public void initDefaultSettings() {
        createIfNotExists("network.scan-range", "192.168.1.0/24", "Network range to scan (CIDR notation)");
        createIfNotExists("network.scanner-tool", "arp-scan", "Scanner tool preference (arp-scan, nmap, ping)");
        createIfNotExists("scheduler.enabled", "true", "Enable scheduled network scans");
        createIfNotExists("scheduler.cron", "0 0 2 * * *", "Cron expression for scheduled scans");
        createIfNotExists("ai.enabled", "false", "Enable AI-powered device identification");
        createIfNotExists("ai.provider", "openai", "AI provider (openai, claude)");
        createIfNotExists("ai.model", "gpt-4", "AI model to use");

        // LangChain4j settings
        createIfNotExists("langchain4j.provider", "gemini", "LangChain4j provider (groq, gemini)");
        createIfNotExists("langchain4j.groq.api-key", "", "Groq API key");
        createIfNotExists("langchain4j.groq.model", "llama-3.3-70b-versatile", "Groq model name");
        createIfNotExists("langchain4j.gemini.api-key", "", "Gemini API key");
        createIfNotExists("langchain4j.gemini.model", "gemini-2.0-flash", "Gemini model name");

        // Samba backup settings
        createIfNotExists("backup.samba.enabled", "false", "Enable backup to Samba/SMB share");
        createIfNotExists("backup.samba.host", "", "Samba server hostname or IP");
        createIfNotExists("backup.samba.share", "", "Samba share name");
        createIfNotExists("backup.samba.path", "", "Path within the share (e.g., /backups/k2so)");
        createIfNotExists("backup.samba.domain", "", "Samba domain (optional)");
        createIfNotExists("backup.samba.username", "", "Samba username");
        createIfNotExists("backup.samba.password", "", "Samba password");
    }

    private void createIfNotExists(String key, String value, String description) {
        if (!appSettingsRepository.existsByKey(key)) {
            AppSettings setting = new AppSettings(key, value, description);
            appSettingsRepository.save(setting);
        }
    }

    public List<AppSettings> getAllSettings() {
        return appSettingsRepository.findAll();
    }

    public Optional<AppSettings> getSetting(String key) {
        return appSettingsRepository.findByKey(key);
    }

    public String getSettingValue(String key, String defaultValue) {
        return appSettingsRepository.findByKey(key)
                .map(AppSettings::getValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void updateSetting(String key, String value) {
        AppSettings setting = appSettingsRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));
        setting.setValue(value);
        appSettingsRepository.save(setting);
    }

    @Transactional
    public void createOrUpdateSetting(String key, String value, String description) {
        AppSettings setting = appSettingsRepository.findByKey(key)
                .orElse(new AppSettings(key, value, description));
        setting.setValue(value);
        if (description != null) {
            setting.setDescription(description);
        }
        appSettingsRepository.save(setting);
    }
}
