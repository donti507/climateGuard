package com.climateguard.api.service;

import com.climateguard.api.dto.request.RegionRequest;
import com.climateguard.api.dto.response.RegionResponse;
import com.climateguard.api.exception.ResourceNotFoundException;
import com.climateguard.api.mapper.RegionMapper;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    /**
     * Returns a paginated list of all regions, optionally filtered by country.
     */
    @Transactional(readOnly = true)
    public Page<RegionResponse> getAllRegions(String country, Pageable pageable) {
        Page<Region> regions = (country != null && !country.isBlank())
                ? regionRepository.findByCountryIgnoreCase(country, pageable)
                : regionRepository.findAll(pageable);
        return regions.map(regionMapper::toResponse);
    }

    /**
     * Retrieves a single region by ID.
     */
    @Transactional(readOnly = true)
    public RegionResponse getRegionById(Long id) {
        return regionRepository.findById(id)
                .map(regionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + id));
    }

    /**
     * Creates a new geographic region.
     */
    @Transactional
    public RegionResponse createRegion(RegionRequest request) {
        Region region = regionMapper.toEntity(request);
        return regionMapper.toResponse(regionRepository.save(region));
    }

    /**
     * Updates an existing region by ID.
     */
    @Transactional
    public RegionResponse updateRegion(Long id, RegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + id));
        regionMapper.updateEntity(request, region);
        return regionMapper.toResponse(regionRepository.save(region));
    }

    /**
     * Deletes a region by ID.
     */
    @Transactional
    public void deleteRegion(Long id) {
        if (!regionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Region not found with id: " + id);
        }
        regionRepository.deleteById(id);
    }
}
