package ccm.admin.report.repository;

import ccm.admin.report.entity.ReportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {

    /** Find reports by generator */
    List<ReportHistory> findByGeneratedByOrderByGeneratedAtDesc(Long generatedBy);

    /** Find reports by type */
    Page<ReportHistory> findByTypeOrderByGeneratedAtDesc(String type, Pageable pageable);

    /** Find recent reports */
    Page<ReportHistory> findAllByOrderByGeneratedAtDesc(Pageable pageable);

    /** Find reports within date range */
    @Query("SELECT r FROM ReportHistory r WHERE r.generatedAt BETWEEN :startDate AND :endDate ORDER BY r.generatedAt DESC")
    List<ReportHistory> findByGeneratedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
