package com.climateguard.api.repository;

import com.climateguard.api.model.FloodEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FloodEventRepository extends JpaRepository<FloodEvent, Long> {
    Page<FloodEvent> findByRegionId(Long regionId, Pageable pageable);

    @Query("SELECT e FROM FloodEvent e WHERE e.riskScore > 0.7 ORDER BY e.riskScore DESC")
    List<FloodEvent> findHighRiskEvents();

    @Query("SELECT e FROM FloodEvent e WHERE " +
           "(:severity IS NULL OR e.severity = :severity) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:minRisk IS NULL OR e.riskScore >= :minRisk)")
    Page<FloodEvent> findFiltered(
            @Param("severity") String severity,
            @Param("eventType") String eventType,
            @Param("minRisk") Double minRisk,
            Pageable pageable);
}
