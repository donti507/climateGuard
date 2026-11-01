package com.climateguard.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.climateguard.api.dto.response.ObservationResponse;
import com.climateguard.api.exception.ResourceNotFoundException;
import com.climateguard.api.mapper.ObservationMapper;
import com.climateguard.api.model.ClimateObservation;
import com.climateguard.api.repository.ObservationRepository;
import com.climateguard.api.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ObservationServiceExportTest {

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ObservationMapper observationMapper;

    private ObservationService observationService;

    @BeforeEach
    void setUp() {
        observationService = new ObservationService(observationRepository, regionRepository, observationMapper);
    }

    @Test
    void getExportByRegionReturnsMappedObservations() {
        Long regionId = 1L;
        LocalDateTime from = LocalDateTime.of(2024, 9, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 9, 30, 23, 59);
        String source = "station";

        ClimateObservation observation = mock(ClimateObservation.class);
        ObservationResponse response = mock(ObservationResponse.class);

        when(regionRepository.existsById(regionId)).thenReturn(true);
        when(observationRepository.findFilteredAll(regionId, from, to, source)).thenReturn(List.of(observation));
        when(observationMapper.toResponse(observation)).thenReturn(response);

        List<ObservationResponse> result = observationService.getExportByRegion(regionId, from, to, source);

        assertEquals(List.of(response), result);
        verify(observationRepository).findFilteredAll(regionId, from, to, source);
    }

    @Test
    void getExportByRegionReturnsEmptyListWhenNoObservationsMatch() {
        Long regionId = 1L;

        when(regionRepository.existsById(regionId)).thenReturn(true);
        when(observationRepository.findFilteredAll(regionId, null, null, null)).thenReturn(List.of());

        List<ObservationResponse> result = observationService.getExportByRegion(regionId, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getExportByRegionThrowsWhenRegionDoesNotExist() {
        Long regionId = 99L;

        when(regionRepository.existsById(regionId)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> observationService.getExportByRegion(regionId, null, null, null)
        );

        verifyNoInteractions(observationRepository);
    }
}
