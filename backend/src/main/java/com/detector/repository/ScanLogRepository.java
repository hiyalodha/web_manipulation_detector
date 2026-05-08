package com.detector.repository;
import com.detector.model.ScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
    List<ScanLog> findByDomainOrderByScannedAtDesc(String domain);
    List<ScanLog> findTop10ByOrderByScannedAtDesc();
    List<ScanLog> findByTotalPatternsFoundGreaterThan(Integer count);
    @Query("SELECT s.domain, AVG(s.pageScore) as avgScore, COUNT(s) as scanCount FROM ScanLog s GROUP BY s.domain ORDER BY avgScore DESC")
    List<Object[]> findTopManipulativeDomains();
}
