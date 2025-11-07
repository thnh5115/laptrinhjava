package ccm.admin.system.settings.repository;

import ccm.admin.system.settings.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/** repository - Service Interface - repository business logic and data operations */

public interface SettingRepository extends JpaRepository<Setting, Long> {
    
    
    Optional<Setting> findByKeyName(String keyName);
}
