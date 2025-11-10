package ccm.owner.repo;

import ccm.owner.entitys.Journey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findByOwnerId(Long id);
}
