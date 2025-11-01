package ccm.admin.system.settings.service;

import ccm.admin.system.settings.dto.request.UpdateSettingRequest;
import ccm.admin.system.settings.dto.response.SettingResponse;

import java.util.List;

/**
 * Service interface for Settings management
 * Handles business logic for system configuration
 */
public interface SettingService {

    /**
     * Get all system settings
     *
     * @return List of all settings
     */
    List<SettingResponse> getAllSettings();

    /**
     * Update a setting value by ID
     *
     * @param id Setting ID
     * @param request Request containing new value
     * @return Updated setting
     * @throws jakarta.persistence.EntityNotFoundException if setting not found
     */
    SettingResponse updateSetting(Long id, UpdateSettingRequest request);

    /**
     * Get a setting by key name
     *
     * @param keyName The unique key name
     * @return Setting response
     * @throws jakarta.persistence.EntityNotFoundException if setting not found
     */
    SettingResponse getSettingByKey(String keyName);
}
