package ccm.admin.system.settings.service;

import ccm.admin.system.settings.dto.request.UpdateSettingRequest;
import ccm.admin.system.settings.dto.response.SettingResponse;

import java.util.List;

/** service - Service Interface - service business logic and data operations */

public interface SettingService {

    
    List<SettingResponse> getAllSettings();

    
    SettingResponse updateSetting(Long id, UpdateSettingRequest request);

    
    SettingResponse getSettingByKey(String keyName);
}
