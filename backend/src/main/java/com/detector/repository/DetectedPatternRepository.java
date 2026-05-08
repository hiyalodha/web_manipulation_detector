package com.detector.repository;
import com.detector.model.DetectedPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetectedPatternRepository extends JpaRepository<DetectedPattern, Long> {
    List<DetectedPattern> findByScanLogId(Long scanId);
    List<DetectedPattern> findByCategory(String category);
    List<DetectedPattern> findByIsFalsePositiveFalse();
    @Query("SELECT d.category, COUNT(d) as total FROM DetectedPattern d GROUP BY d.category ORDER BY total DESC")
    List<Object[]> countByCategory();
    @Query("SELECT d.patternText, COUNT(d) as occurrences FROM DetectedPattern d GROUP BY d.patternText ORDER BY occurrences DESC")
    List<Object[]> findMostCommonPatterns();
}
