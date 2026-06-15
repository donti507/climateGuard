package com.climateguard.api.repository;

import com.climateguard.api.model.ClimateObservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ObservationRepository extends JpaRepository<ClimateObservation, Long> {
    Page<ClimateObservation> findByRegionId(Long regionId, Pageable pageable);

    List<ClimateObservation> findByRegionIdAndObservedAtBetween(
            Long regionId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT o FROM ClimateObservation o WHERE o.region.id = :regionId " +
           "AND (:from IS NULL OR o.observedAt >= :from) " +
           "AND (:to IS NULL OR o.observedAt <= :to) " +
           "AND (:source IS NULL OR o.source = :source)")
    Page<ClimateObservation> findFiltered(
            @Param("regionId") Long regionId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("source") String source,
            Pageable pageable);
}
