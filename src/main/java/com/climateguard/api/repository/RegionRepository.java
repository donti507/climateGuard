package com.climateguard.api.repository;

import com.climateguard.api.model.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Page<Region> findByCountryIgnoreCase(String country, Pageable pageable);
}
