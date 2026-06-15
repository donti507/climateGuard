package com.climateguard.api.service;

import com.climateguard.api.dto.request.FloodEventRequest;
import com.climateguard.api.dto.response.FloodEventResponse;
import com.climateguard.api.exception.ResourceNotFoundException;
import com.climateguard.api.mapper.FloodEventMapper;
import com.climateguard.api.model.FloodEvent;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.FloodEventRepository;
import com.climateguard.api.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FloodEventService {

    private final FloodEventRepository floodEventRepository;
    private final RegionRepository regionRepository;
    private final FloodEventMapper floodEventMapper;

    /**
     * Returns a paginated and filtered list of flood events.
     */
    @Transactional(readOnly = true)
    public Page<FloodEventResponse> getAll(String severity, String eventType, Double minRisk, Pageable pageable) {
        return floodEventRepository.findFiltered(severity, eventType, minRisk, pageable)
                .map(floodEventMapper::toResponse);
    }

    /**
     * Returns a single flood event by ID.
     */
    @Transactional(readOnly = true)
    public FloodEventResponse getById(Long id) {
        return floodEventRepository.findById(id)
                .map(floodEventMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Flood event not found with id: " + id));
    }

    /**
     * Returns all flood events for a specific region.
     */
    @Transactional(readOnly = true)
    public Page<FloodEventResponse> getByRegion(Long regionId, Pageable pageable) {
        if (!regionRepository.existsById(regionId)) {
            throw new ResourceNotFoundException("Region not found with id: " + regionId);
        }
        return floodEventRepository.findByRegionId(regionId, pageable)
                .map(floodEventMapper::toResponse);
    }

    /**
     * Returns all flood events with risk score above 0.7 (high risk threshold).
     */
    @Transactional(readOnly = true)
    public List<FloodEventResponse> getHighRiskEvents() {
        return floodEventRepository.findHighRiskEvents().stream()
                .map(floodEventMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new flood event record.
     */
    @Transactional
    public FloodEventResponse create(FloodEventRequest request) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + request.getRegionId()));
        FloodEvent event = FloodEvent.builder()
                .region(region)
                .eventType(request.getEventType())
                .severity(request.getSeverity())
                .riskScore(request.getRiskScore())
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .description(request.getDescription())
                .build();
        return floodEventMapper.toResponse(floodEventRepository.save(event));
    }

    /**
     * Updates an existing flood event.
     */
    @Transactional
    public FloodEventResponse update(Long id, FloodEventRequest request) {
        FloodEvent event = floodEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flood event not found with id: " + id));
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + request.getRegionId()));
        event.setRegion(region);
        event.setEventType(request.getEventType());
        event.setSeverity(request.getSeverity());
        event.setRiskScore(request.getRiskScore());
        event.setStartedAt(request.getStartedAt());
        event.setEndedAt(request.getEndedAt());
        event.setDescription(request.getDescription());
        return floodEventMapper.toResponse(floodEventRepository.save(event));
    }

    /**
     * Deletes a flood event by ID.
     */
    @Transactional
    public void delete(Long id) {
        if (!floodEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flood event not found with id: " + id);
        }
        floodEventRepository.deleteById(id);
    }
}
