package ccm.cva.shared.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
        List<OutboxStatus> statuses,
        Instant threshold,
        Pageable pageable
    );
}
