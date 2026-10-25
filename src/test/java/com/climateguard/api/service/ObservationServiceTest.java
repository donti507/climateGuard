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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObservationServiceTest {

    @Mock private ObservationRepository observationRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private ObservationMapper observationMapper;
    @InjectMocks private ObservationService observationService;

    private ClimateObservation observation;
    private ObservationResponse observationResponse;
    private ObservationRequest observationRequest;
    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
                .id(1L).name("Siechnice").country("Poland")
                .build();
        observation = ClimateObservation.builder()
                .id(1L)
                .region(region)
                .observedAt(LocalDateTime.of(2024, 6, 15, 12, 0))
                .temperatureC(25.5)
                .rainfallMm(10.0)
                .humidityPct(70.0)
                .windSpeedMs(5.2)
                .source("METAR")
                .build();
        observationResponse = new ObservationResponse();
        observationResponse.setId(1L);
        observationResponse.setRegionId(1L);
        observationResponse.setRegionName("Siechnice");
        observationResponse.setObservedAt(LocalDateTime.of(2024, 6, 15, 12, 0));
        observationResponse.setTemperatureC(25.5);
        observationResponse.setRainfallMm(10.0);
        observationResponse.setHumidityPct(70.0);
        observationResponse.setWindSpeedMs(5.2);
        observationResponse.setSource("METAR");
        observationRequest = new ObservationRequest();
        observationRequest.setRegionId(1L);
        observationRequest.setObservedAt(LocalDateTime.of(2024, 6, 15, 12, 0));
        observationRequest.setTemperatureC(25.5);
        observationRequest.setRainfallMm(10.0);
        observationRequest.setHumidityPct(70.0);
        observationRequest.setWindSpeedMs(5.2);
        observationRequest.setSource("METAR");
    }

    @Test
    void getAll_shouldReturnPaginatedObservations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClimateObservation> page = new PageImpl<>(List.of(observation));
        when(observationRepository.findAll(pageable)).thenReturn(page);
        when(observationMapper.toResponse(observation)).thenReturn(observationResponse);

        Page<ObservationResponse> result = observationService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSource()).isEqualTo("METAR");
        verify(observationRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoData() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClimateObservation> page = new PageImpl<>(Collections.emptyList());
        when(observationRepository.findAll(pageable)).thenReturn(page);

        Page<ObservationResponse> result = observationService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getById_shouldReturnObservation_whenExists() {
        when(observationRepository.findById(1L)).thenReturn(Optional.of(observation));
        when(observationMapper.toResponse(observation)).thenReturn(observationResponse);

        ObservationResponse result = observationService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTemperatureC()).isEqualTo(25.5);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(observationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByRegion_shouldReturnFilteredObservations_whenRegionExists() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime from = LocalDateTime.of(2024, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 6, 30, 23, 59);
        Page<ClimateObservation> page = new PageImpl<>(List.of(observation));

        when(regionRepository.existsById(1L)).thenReturn(true);
        when(observationRepository.findFiltered(1L, from, to, "METAR", pageable)).thenReturn(page);
        when(observationMapper.toResponse(observation)).thenReturn(observationResponse);

        Page<ObservationResponse> result = observationService.getByRegion(1L, from, to, "METAR", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(regionRepository).existsById(1L);
        verify(observationRepository).findFiltered(1L, from, to, "METAR", pageable);
    }

    @Test
    void getByRegion_shouldReturnEmptyPage_whenNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClimateObservation> page = new PageImpl<>(Collections.emptyList());

        when(regionRepository.existsById(1L)).thenReturn(true);
        when(observationRepository.findFiltered(eq(1L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Page<ObservationResponse> result = observationService.getByRegion(1L, null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getByRegion_shouldThrow_whenRegionNotFound() {
        when(regionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> observationService.getByRegion(99L, null, null, null, Pageable.ofSize(10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getStatsForRegion_shouldCalculateStats_whenObservationsExist() {
        LocalDateTime from = LocalDateTime.of(2024, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 6, 30, 23, 59);

        ClimateObservation obs1 = ClimateObservation.builder()
                .id(1L).region(region)
                .temperatureC(20.0).rainfallMm(5.0).humidityPct(60.0).windSpeedMs(3.0)
                .build();
        ClimateObservation obs2 = ClimateObservation.builder()
                .id(2L).region(region)
                .temperatureC(30.0).rainfallMm(15.0).humidityPct(80.0).windSpeedMs(7.0)
                .build();

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(observationRepository.findByRegionIdAndObservedAtBetween(1L, from, to))
                .thenReturn(List.of(obs1, obs2));

        ObservationStatsResponse result = observationService.getStatsForRegion(1L, from, to);

        assertThat(result).isNotNull();
        assertThat(result.getRegionId()).isEqualTo(1L);
        assertThat(result.getRegionName()).isEqualTo("Siechnice");
        assertThat(result.getObservationCount()).isEqualTo(2L);
        assertThat(result.getAvgTemperatureC()).isEqualTo(25.0);
        assertThat(result.getMaxTemperatureC()).isEqualTo(30.0);
        assertThat(result.getMinTemperatureC()).isEqualTo(20.0);
        assertThat(result.getTotalRainfallMm()).isEqualTo(20.0);
        assertThat(result.getAvgHumidityPct()).isEqualTo(70.0);
        assertThat(result.getAvgWindSpeedMs()).isEqualTo(5.0);
    }

    @Test
    void getStatsForRegion_shouldReturnZeroStats_whenNoObservations() {
        LocalDateTime from = LocalDateTime.of(2024, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 6, 30, 23, 59);

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(observationRepository.findByRegionIdAndObservedAtBetween(1L, from, to))
                .thenReturn(Collections.emptyList());

        ObservationStatsResponse result = observationService.getStatsForRegion(1L, from, to);

        assertThat(result).isNotNull();
        assertThat(result.getObservationCount()).isEqualTo(0L);
        assertThat(result.getAvgTemperatureC()).isEqualTo(0.0);
        assertThat(result.getMaxTemperatureC()).isEqualTo(0.0);
        assertThat(result.getMinTemperatureC()).isEqualTo(0.0);
        assertThat(result.getTotalRainfallMm()).isEqualTo(0.0);
        assertThat(result.getAvgHumidityPct()).isEqualTo(0.0);
        assertThat(result.getAvgWindSpeedMs()).isEqualTo(0.0);
    }

    @Test
    void getStatsForRegion_shouldThrow_whenRegionNotFound() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.getStatsForRegion(99L, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveAndReturn() {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(observationRepository.save(any(ClimateObservation.class))).thenReturn(observation);
        when(observationMapper.toResponse(observation)).thenReturn(observationResponse);

        ObservationResponse result = observationService.create(observationRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSource()).isEqualTo("METAR");
        verify(regionRepository).findById(1L);
        verify(observationRepository, times(1)).save(any(ClimateObservation.class));
    }

    @Test
    void create_shouldThrow_whenRegionNotFound() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());
        observationRequest.setRegionId(99L);

        assertThatThrownBy(() -> observationService.create(observationRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(observationRepository, never()).save(any(ClimateObservation.class));
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(observationRepository.existsById(1L)).thenReturn(true);

        observationService.delete(1L);

        verify(observationRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(observationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> observationService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(observationRepository, never()).deleteById(anyLong());
    }
}
