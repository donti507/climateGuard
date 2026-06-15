package com.climateguard.api.service;

import com.climateguard.api.dto.request.ObservationRequest;
import com.climateguard.api.dto.response.ObservationResponse;
import com.climateguard.api.dto.response.ObservationStatsResponse;
import com.climateguard.api.exception.ResourceNotFoundException;
import com.climateguard.api.mapper.ObservationMapper;
import com.climateguard.api.model.ClimateObservation;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.ObservationRepository;
import com.climateguard.api.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final RegionRepository regionRepository;
    private final ObservationMapper observationMapper;

    /**
     * Returns a paginated list of all observations with optional filters.
     */
    @Transactional(readOnly = true)
    public Page<ObservationResponse> getAll(Pageable pageable) {
        return observationRepository.findAll(pageable).map(observationMapper::toResponse);
    }

    /**
     * Returns a single observation by ID.
     */
    @Transactional(readOnly = true)
    public ObservationResponse getById(Long id) {
        return observationRepository.findById(id)
                .map(observationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Observation not found with id: " + id));
    }

    /**
     * Returns paginated observations for a specific region with optional filters.
     */
    @Transactional(readOnly = true)
    public Page<ObservationResponse> getByRegion(Long regionId, LocalDateTime from,
                                                  LocalDateTime to, String source, Pageable pageable) {
        if (!regionRepository.existsById(regionId)) {
            throw new ResourceNotFoundException("Region not found with id: " + regionId);
        }
        return observationRepository
                .findFiltered(regionId, from, to, source, pageable)
                .map(observationMapper::toResponse);
    }

    /**
     * Calculates aggregate climate statistics for a region over a time period.
     */
    @Transactional(readOnly = true)
    public ObservationStatsResponse getStatsForRegion(Long regionId, LocalDateTime from, LocalDateTime to) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + regionId));

        LocalDateTime start = (from != null) ? from : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = (to != null) ? to : LocalDateTime.now();

        List<ClimateObservation> observations = observationRepository
                .findByRegionIdAndObservedAtBetween(regionId, start, end);

        ObservationStatsResponse stats = new ObservationStatsResponse();
        stats.setRegionId(regionId);
        stats.setRegionName(region.getName());
        stats.setPeriodStart(start);
        stats.setPeriodEnd(end);
        stats.setObservationCount((long) observations.size());

        if (!observations.isEmpty()) {
            stats.setAvgTemperatureC(observations.stream()
                    .filter(o -> o.getTemperatureC() != null)
                    .mapToDouble(o -> o.getTemperatureC().doubleValue())
                    .average().orElse(0.0));
            stats.setMaxTemperatureC(observations.stream()
                    .filter(o -> o.getTemperatureC() != null)
                    .mapToDouble(o -> o.getTemperatureC().doubleValue())
                    .max().orElse(0.0));
            stats.setMinTemperatureC(observations.stream()
                    .filter(o -> o.getTemperatureC() != null)
                    .mapToDouble(o -> o.getTemperatureC().doubleValue())
                    .min().orElse(0.0));
            stats.setTotalRainfallMm(observations.stream()
                    .filter(o -> o.getRainfallMm() != null)
                    .mapToDouble(o -> o.getRainfallMm().doubleValue())
                    .sum());
            stats.setAvgHumidityPct(observations.stream()
                    .filter(o -> o.getHumidityPct() != null)
                    .mapToDouble(o -> o.getHumidityPct().doubleValue())
                    .average().orElse(0.0));
            stats.setAvgWindSpeedMs(observations.stream()
                    .filter(o -> o.getWindSpeedMs() != null)
                    .mapToDouble(o -> o.getWindSpeedMs().doubleValue())
                    .average().orElse(0.0));
        }
        return stats;
    }

    /**
     * Creates a new climate observation.
     */
    @Transactional
    public ObservationResponse create(ObservationRequest request) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + request.getRegionId()));
        ClimateObservation obs = ClimateObservation.builder()
                .region(region)
                .observedAt(request.getObservedAt())
                .temperatureC(request.getTemperatureC())
                .rainfallMm(request.getRainfallMm())
                .humidityPct(request.getHumidityPct())
                .windSpeedMs(request.getWindSpeedMs())
                .source(request.getSource())
                .build();
        return observationMapper.toResponse(observationRepository.save(obs));
    }

    /**
     * Deletes an observation by ID.
     */
    @Transactional
    public void delete(Long id) {
        if (!observationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Observation not found with id: " + id);
        }
        observationRepository.deleteById(id);
    }
}
