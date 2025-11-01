package ccm.admin.system.settings.controller;

import ccm.admin.system.settings.dto.request.UpdateSettingRequest;
import ccm.admin.system.settings.dto.response.SettingResponse;
import ccm.admin.system.settings.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Settings Management (Admin Only)
 * Provides endpoints for viewing and updating system configuration
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SettingAdminController {

    private final SettingService settingService;

    /**
     * Get all system settings
     *
     * @return List of all settings with their current values
     */
    @GetMapping
    public List<SettingResponse> getSettings() {
        log.info("GET /api/admin/settings - Fetching all settings");
        return settingService.getAllSettings();
    }

    /**
     * Get a specific setting by key name
     *
     * @param keyName The unique key name of the setting
     * @return Setting details
     */
    @GetMapping("/key/{keyName}")
    public SettingResponse getSettingByKey(@PathVariable String keyName) {
        log.info("GET /api/admin/settings/key/{} - Fetching setting by key", keyName);
        return settingService.getSettingByKey(keyName);
    }

    /**
     * Update a setting value
     * Changes take effect immediately without server restart
     *
     * @param id Setting ID
     * @param request Request containing new value
     * @return Updated setting
     */
    @PutMapping("/{id}")
    public SettingResponse updateSetting(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSettingRequest request
    ) {
        log.info("PUT /api/admin/settings/{} - Updating setting value", id);
        return settingService.updateSetting(id, request);
    }
}
