package ccm.admin.system.settings.repository;

import ccm.admin.system.settings.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Setting entity
 * Provides access to system configuration settings
 */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    
    /**
     * Find a setting by its key name
     * Used for retrieving specific configuration values
     *
     * @param keyName The unique key name of the setting
     * @return Optional containing the setting if found
     */
    Optional<Setting> findByKeyName(String keyName);
}
