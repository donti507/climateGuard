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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObservationServiceTest {

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ObservationMapper observationMapper;

    @InjectMocks
    private ObservationService observationService;

    private Region region1;
    private ClimateObservation observation1;
    private ClimateObservation observation2;
    private ObservationResponse observationResponse1;
    private ObservationResponse observationResponse2;
    private ObservationRequest observationRequest;

    @BeforeEach
    void setUp() {
        region1 = Region.builder().id(1L).name("Test Region").build();

        LocalDateTime now = LocalDateTime.now();
        observation1 = ClimateObservation.builder()
                .id(1L)
                .region(region1)
                .observedAt(now.minusHours(1))
                .temperatureC(BigDecimal.valueOf(25.5))
                .rainfallMm(BigDecimal.valueOf(5.2))
                .humidityPct(BigDecimal.valueOf(60.0))
                .windSpeedMs(BigDecimal.valueOf(3.0))
                .source("WeatherAPI")
                .build();

        observation2 = ClimateObservation.builder()
                .id(2L)
                .region(region1)
                .observedAt(now.minusHours(2))
                .temperatureC(BigDecimal.valueOf(24.0))
                .rainfallMm(BigDecimal.valueOf(2.1))
                .humidityPct(BigDecimal.valueOf(65.0))
                .windSpeedMs(BigDecimal.valueOf(4.0))
                .source("LocalSensor")
                .build();

        observationResponse1 = ObservationResponse.builder()
                .id(1L)
                .regionId(region1.getId())
                .observedAt(observation1.getObservedAt())
                .temperatureC(BigDecimal.valueOf(25.5))
                .rainfallMm(BigDecimal.valueOf(5.2))
                .humidityPct(BigDecimal.valueOf(60.0))
                .windSpeedMs(BigDecimal.valueOf(3.0))
                .source("WeatherAPI")
                .build();

        observationResponse2 = ObservationResponse.builder()
                .id(2L)
                .regionId(region1.getId())
                .observedAt(observation2.getObservedAt())
                .temperatureC(BigDecimal.valueOf(24.0))
                .rainfallMm(BigDecimal.valueOf(2.1))
                .humidityPct(BigDecimal.valueOf(65.0))
                .windSpeedMs(BigDecimal.valueOf(4.0))
                .source("LocalSensor")
                .build();

        observationRequest = ObservationRequest.builder()
                .regionId(region1.getId())
                .observedAt(now.minusHours(1))
                .temperatureC(BigDecimal.valueOf(25.5))
                .rainfallMm(BigDecimal.valueOf(5.2))
                .humidityPct(BigDecimal.valueOf(60.0))
                .windSpeedMs(BigDecimal.valueOf(3.0))
                .source("WeatherAPI")
                .build();
    }

    @Test
    void getAll_shouldReturnPageOfObservations() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ClimateObservation> observations = Arrays.asList(observation1, observation2);
        Page<ClimateObservation> page = new PageImpl<>(observations, pageable, observations.size());

        when(observationRepository.findAll(pageable)).thenReturn(page);
        when(observationMapper.toResponse(any(ClimateObservation.class)))
                .thenReturn(observationResponse1, observationResponse2);

        Page<ObservationResponse> result = observationService.getAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(observationResponse1.getId(), result.getContent().get(0).getId());
        assertEquals(observationResponse2.getId(), result.getContent().get(1).getId());

        verify(observationRepository).findAll(pageable);
        verify(observationMapper, times(2)).toResponse(any(ClimateObservation.class));
    }

    @Test
    void getById_shouldReturnObservationResponse() {
        when(observationRepository.findById(1L)).thenReturn(Optional.of(observation1));
        when(observationMapper.toResponse(observation1)).thenReturn(observationResponse1);

        ObservationResponse result = observationService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(observationResponse1.getSource(), result.getSource());

        verify(observationRepository).findById(1L);
        verify(observationMapper).toResponse(observation1);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException() {
        when(observationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> observationService.getById(99L));

        verify(observationRepository).findById(99L);
        verify(observationMapper, never()).toResponse(any(ClimateObservation.class));
    }

    @Test
    void getByRegion_shouldReturnPageOfObservationsWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        String source = "WeatherAPI";
        List<ClimateObservation> observations = Arrays.asList(observation1);
        Page<ClimateObservation> page = new PageImpl<>(observations, pageable, observations.size());

        when(regionRepository.existsById(1L)).thenReturn(true);
        when(observationRepository.findFiltered(1L, from, to, source, pageable)).thenReturn(page);
        when(observationMapper.toResponse(observation1)).thenReturn(observationResponse1);

        Page<ObservationResponse> result = observationService.getByRegion(1L, from, to, source, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(observationResponse1.getId(), result.getContent().get(0).getId());

        verify(regionRepository).existsById(1L);
        verify(observationRepository).findFiltered(1L, from, to, source, pageable);
        verify(observationMapper).toResponse(observation1);
    }

    @Test
    void getByRegion_shouldThrowResourceNotFoundExceptionForRegion() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        when(regionRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> observationService.getByRegion(99L, from, to, null, pageable));

        verify(regionRepository).existsById(99L);
        verify(observationRepository, never()).findFiltered(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyString(), any(Pageable.class));
        verify(observationMapper, never()).toResponse(any(ClimateObservation.class));
    }

    @Test
    void getStatsForRegion_shouldReturnObservationStatsResponse() {
        LocalDateTime from = LocalDateTime.now().minusMonths(1);
        LocalDateTime to = LocalDateTime.now();
        List<ClimateObservation> observations = Arrays.asList(observation1, observation2);

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region1));
        when(observationRepository.findByRegionIdAndObservedAtBetween(1L, from, to))
                .thenReturn(observations);

        ObservationStatsResponse stats = observationService.getStatsForRegion(1L, from, to);

        assertNotNull(stats);
        assertEquals(1L, stats.getRegionId());
        assertEquals("Test Region", stats.getRegionName());
        assertEquals(2, stats.getObservationCount());
        // Calculate expected values using BigDecimal for precision
        BigDecimal expectedAvgTemp = BigDecimal.valueOf(25.5).add(BigDecimal.valueOf(24.0)).divide(BigDecimal.valueOf(2));
        assertEquals(expectedAvgTemp.doubleValue(), stats.getAvgTemperatureC());
        assertEquals(25.5, stats.getMaxTemperatureC());
        assertEquals(24.0, stats.getMinTemperatureC());
        BigDecimal expectedTotalRain = BigDecimal.valueOf(5.2).add(BigDecimal.valueOf(2.1));
        assertEquals(expectedTotalRain.doubleValue(), stats.getTotalRainfallMm());
        BigDecimal expectedAvgHumidity = BigDecimal.valueOf(60.0).add(BigDecimal.valueOf(65.0)).divide(BigDecimal.valueOf(2));
        assertEquals(expectedAvgHumidity.doubleValue(), stats.getAvgHumidityPct());
        BigDecimal expectedAvgWind = BigDecimal.valueOf(3.0).add(BigDecimal.valueOf(4.0)).divide(BigDecimal.valueOf(2));
        assertEquals(expectedAvgWind.doubleValue(), stats.getAvgWindSpeedMs());

        verify(regionRepository).findById(1L);
        verify(observationRepository).findByRegionIdAndObservedAtBetween(1L, from, to);
    }

    @Test
    void getStatsForRegion_shouldReturnObservationStatsResponseWithDefaultDateRange() {
        List<ClimateObservation> observations = Arrays.asList(observation1, observation2);

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region1));
        // Mocking for the default date range, which is roughly the last month.
        when(observationRepository.findByRegionIdAndObservedAtBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(observations);

        ObservationStatsResponse stats = observationService.getStatsForRegion(1L, null, null);

        assertNotNull(stats);
        assertEquals(1L, stats.getRegionId());
        assertEquals("Test Region", stats.getRegionName());
        assertEquals(2, stats.getObservationCount());
        verify(regionRepository).findById(1L);
        verify(observationRepository).findByRegionIdAndObservedAtBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getStatsForRegion_shouldReturnZeroStatsForEmptyObservations() {
        LocalDateTime from = LocalDateTime.now().minusMonths(1);
        LocalDateTime to = LocalDateTime.now();
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region1));
        when(observationRepository.findByRegionIdAndObservedAtBetween(1L, from, to))
                .thenReturn(Collections.emptyList());

        ObservationStatsResponse stats = observationService.getStatsForRegion(1L, from, to);

        assertNotNull(stats);
        assertEquals(1L, stats.getRegionId());
        assertEquals("Test Region", stats.getRegionName());
        assertEquals(0, stats.getObservationCount());
        assertEquals(0.0, stats.getAvgTemperatureC());
        assertEquals(0.0, stats.getMaxTemperatureC());
        assertEquals(0.0, stats.getMinTemperatureC());
        assertEquals(0.0, stats.getTotalRainfallMm());
        assertEquals(0.0, stats.getAvgHumidityPct());
        assertEquals(0.0, stats.getAvgWindSpeedMs());

        verify(regionRepository).findById(1L);
        verify(observationRepository).findByRegionIdAndObservedAtBetween(1L, from, to);
    }

    @Test
    void getStatsForRegion_shouldThrowResourceNotFoundExceptionForRegion() {
        LocalDateTime from = LocalDateTime.now().minusMonths(1);
        LocalDateTime to = LocalDateTime.now();
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> observationService.getStatsForRegion(99L, from, to));

        verify(regionRepository).findById(99L);
        verify(observationRepository, never()).findByRegionIdAndObservedAtBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void create_shouldReturnCreatedObservationResponse() {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region1));
        when(observationRepository.save(any(ClimateObservation.class))).thenReturn(observation1);
        when(observationMapper.toResponse(observation1)).thenReturn(observationResponse1);

        ObservationResponse result = observationService.create(observationRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(observationResponse1.getSource(), result.getSource());

        verify(regionRepository).findById(1L);
        verify(observationRepository).save(any(ClimateObservation.class));
        verify(observationMapper).toResponse(observation1);
    }

    @Test
    void create_shouldThrowResourceNotFoundExceptionForRegion() {
        ObservationRequest invalidRequest = ObservationRequest.builder()
                .regionId(99L) // Non-existent region
                .observedAt(LocalDateTime.now())
                .temperatureC(BigDecimal.valueOf(20.0))
                .rainfallMm(BigDecimal.valueOf(0.0))
                .humidityPct(BigDecimal.valueOf(50.0))
                .windSpeedMs(BigDecimal.valueOf(1.0))
                .build();

        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> observationService.create(invalidRequest));

        verify(regionRepository).findById(99L);
        verify(observationRepository, never()).save(any(ClimateObservation.class));
        verify(observationMapper, never()).toResponse(any(ClimateObservation.class));
    }

    @Test
    void delete_shouldSucceed() {
        when(observationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(observationRepository).deleteById(1L);

        observationService.delete(1L);

        verify(observationRepository).existsById(1L);
        verify(observationRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException() {
        when(observationRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> observationService.delete(99L));

        verify(observationRepository).existsById(99L);
        verify(observationRepository, never()).deleteById(anyLong());
    }
}
