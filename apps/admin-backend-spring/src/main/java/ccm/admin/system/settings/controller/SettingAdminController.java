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

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
/** Settings - REST Controller - Admin endpoints for Settings management */

public class SettingAdminController {

    private final SettingService settingService;

    
    /** GET /api/admin/settings - perform operation */
    @GetMapping
    public List<SettingResponse> getSettings() {
        log.info("GET /api/admin/settings - Fetching all settings");
        return settingService.getAllSettings();
    }

    
    @GetMapping("/key/{keyName}")
    public SettingResponse getSettingByKey(@PathVariable("keyName") String keyName) {
        log.info("GET /api/admin/settings/key/{} - Fetching setting by key", keyName);
        return settingService.getSettingByKey(keyName);
    }

    
    @PutMapping("/{id}")
    public SettingResponse updateSetting(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateSettingRequest request
    ) {
        log.info("PUT /api/admin/settings/{} - Updating setting value", id);
        return settingService.updateSetting(id, request);
    }
}
