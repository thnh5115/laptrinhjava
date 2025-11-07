package ccm.admin.system.settings.service.impl;

import ccm.admin.system.settings.dto.request.UpdateSettingRequest;
import ccm.admin.system.settings.dto.response.SettingResponse;
import ccm.admin.system.settings.entity.Setting;
import ccm.admin.system.settings.repository.SettingRepository;
import ccm.admin.system.settings.service.SettingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/** Settings - Service Implementation - Business logic for Settings operations */

public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;

    /** Get all records - transactional */
    @Override
    @Transactional(readOnly = true)
    public List<SettingResponse> getAllSettings() {
        log.info("Fetching all system settings");

        List<Setting> settings = settingRepository.findAll();
        List<SettingResponse> responses = settings.stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Found {} settings", responses.size());
        return responses;
    }

    /** Update existing record - transactional */
    @Override
    @Transactional
    public SettingResponse updateSetting(Long id, UpdateSettingRequest request) {
        log.info("Updating setting with ID: {}", id);

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setting not found with ID: " + id));

        String oldValue = setting.getValue();
        setting.setValue(request.getValue());
        setting.setUpdatedAt(LocalDateTime.now());

        settingRepository.save(setting);

        log.info("Setting '{}' updated: {} -> {}", 
                setting.getKeyName(), oldValue, request.getValue());

        return mapToResponse(setting);
    }

    /** Process business logic - transactional */
    @Override
    @Transactional(readOnly = true)
    public SettingResponse getSettingByKey(String keyName) {
        log.info("Fetching setting by key: {}", keyName);

        Setting setting = settingRepository.findByKeyName(keyName)
                .orElseThrow(() -> new EntityNotFoundException("Setting not found with key: " + keyName));

        return mapToResponse(setting);
    }

    
    private SettingResponse mapToResponse(Setting setting) {
        return SettingResponse.builder()
                .id(setting.getId())
                .keyName(setting.getKeyName())
                .value(setting.getValue())
                .description(setting.getDescription())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
