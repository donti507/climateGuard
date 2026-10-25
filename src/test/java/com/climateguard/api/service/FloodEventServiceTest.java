package com.climateguard.api.service;

import com.climateguard.api.dto.request.FloodEventRequest;
import com.climateguard.api.dto.response.FloodEventResponse;
import com.climateguard.api.exception.ResourceNotFoundException;
import com.climateguard.api.mapper.FloodEventMapper;
import com.climateguard.api.model.FloodEvent;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.FloodEventRepository;
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
class FloodEventServiceTest {

    @Mock private FloodEventRepository floodEventRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private FloodEventMapper floodEventMapper;
    @InjectMocks private FloodEventService floodEventService;

    private FloodEvent floodEvent;
    private FloodEventResponse floodEventResponse;
    private FloodEventRequest floodEventRequest;
    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
                .id(1L).name("Siechnice").country("Poland")
                .build();
        floodEvent = FloodEvent.builder()
                .id(1L)
                .region(region)
                .eventType("FLASH_FLOOD")
                .severity("HIGH")
                .riskScore(0.85)
                .startedAt(LocalDateTime.of(2024, 6, 15, 10, 0))
                .endedAt(LocalDateTime.of(2024, 6, 15, 18, 0))
                .description("Severe flash flood")
                .build();
        floodEventResponse = new FloodEventResponse();
        floodEventResponse.setId(1L);
        floodEventResponse.setRegionId(1L);
        floodEventResponse.setRegionName("Siechnice");
        floodEventResponse.setEventType("FLASH_FLOOD");
        floodEventResponse.setSeverity("HIGH");
        floodEventResponse.setRiskScore(0.85);
        floodEventResponse.setStartedAt(LocalDateTime.of(2024, 6, 15, 10, 0));
        floodEventResponse.setEndedAt(LocalDateTime.of(2024, 6, 15, 18, 0));
        floodEventResponse.setDescription("Severe flash flood");
        floodEventRequest = new FloodEventRequest();
        floodEventRequest.setRegionId(1L);
        floodEventRequest.setEventType("FLASH_FLOOD");
        floodEventRequest.setSeverity("HIGH");
        floodEventRequest.setRiskScore(0.85);
        floodEventRequest.setStartedAt(LocalDateTime.of(2024, 6, 15, 10, 0));
        floodEventRequest.setEndedAt(LocalDateTime.of(2024, 6, 15, 18, 0));
        floodEventRequest.setDescription("Severe flash flood");
    }

    @Test
    void getAll_shouldReturnFilteredEvents() {
        Page<FloodEvent> page = new PageImpl<>(List.of(floodEvent));
        when(floodEventRepository.findFiltered("HIGH", "FLASH_FLOOD", 0.5, Pageable.ofSize(10)))
                .thenReturn(page);
        when(floodEventMapper.toResponse(floodEvent)).thenReturn(floodEventResponse);

        Page<FloodEventResponse> result = floodEventService.getAll("HIGH", "FLASH_FLOOD", 0.5, Pageable.ofSize(10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventType()).isEqualTo("FLASH_FLOOD");
        verify(floodEventRepository).findFiltered("HIGH", "FLASH_FLOOD", 0.5, Pageable.ofSize(10));
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoMatches() {
        Page<FloodEvent> page = new PageImpl<>(Collections.emptyList());
        when(floodEventRepository.findFiltered(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<FloodEventResponse> result = floodEventService.getAll("LOW", "DROUGHT", 0.9, Pageable.ofSize(10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getById_shouldReturnEvent_whenExists() {
        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(floodEvent));
        when(floodEventMapper.toResponse(floodEvent)).thenReturn(floodEventResponse);

        FloodEventResponse result = floodEventService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEventType()).isEqualTo("FLASH_FLOOD");
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(floodEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> floodEventService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByRegion_shouldReturnEvents_whenRegionExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FloodEvent> page = new PageImpl<>(List.of(floodEvent));
        when(regionRepository.existsById(1L)).thenReturn(true);
        when(floodEventRepository.findByRegionId(1L, pageable)).thenReturn(page);
        when(floodEventMapper.toResponse(floodEvent)).thenReturn(floodEventResponse);

        Page<FloodEventResponse> result = floodEventService.getByRegion(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(regionRepository).existsById(1L);
        verify(floodEventRepository).findByRegionId(1L, pageable);
    }

    @Test
    void getByRegion_shouldThrow_whenRegionNotFound() {
        when(regionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> floodEventService.getByRegion(99L, Pageable.ofSize(10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getHighRiskEvents_shouldReturnList() {
        when(floodEventRepository.findHighRiskEvents()).thenReturn(List.of(floodEvent));
        when(floodEventMapper.toResponse(floodEvent)).thenReturn(floodEventResponse);

        List<FloodEventResponse> result = floodEventService.getHighRiskEvents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRiskScore()).isEqualTo(0.85);
    }

    @Test
    void getHighRiskEvents_shouldReturnEmptyList_whenNone() {
        when(floodEventRepository.findHighRiskEvents()).thenReturn(Collections.emptyList());

        List<FloodEventResponse> result = floodEventService.getHighRiskEvents();

        assertThat(result).isEmpty();
    }

    @Test
    void create_shouldSaveAndReturn() {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(floodEventRepository.save(any(FloodEvent.class))).thenReturn(floodEvent);
        when(floodEventMapper.toResponse(floodEvent)).thenReturn(floodEventResponse);

        FloodEventResponse result = floodEventService.create(floodEventRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEventType()).isEqualTo("FLASH_FLOOD");
        verify(regionRepository).findById(1L);
        verify(floodEventRepository, times(1)).save(any(FloodEvent.class));
    }

    @Test
    void create_shouldThrow_whenRegionNotFound() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        floodEventRequest.setRegionId(99L);

        assertThatThrownBy(() -> floodEventService.create(floodEventRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(floodEventRepository, never()).save(any(FloodEvent.class));
    }

    @Test
    void update_shouldUpdateAndReturn() {
        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(floodEvent));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(floodEventRepository.save(any(FloodEvent.class))).thenReturn(floodEvent);
        when(floodEventMapper.toResponse(floodEvent)).thenReturn(floodEventResponse);

        FloodEventResponse result = floodEventService.update(1L, floodEventRequest);

        assertThat(result).isNotNull();
        verify(floodEventRepository).findById(1L);
        verify(regionRepository).findById(1L);
        verify(floodEventRepository, times(1)).save(any(FloodEvent.class));
    }

    @Test
    void update_shouldThrow_whenEventNotFound() {
        when(floodEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> floodEventService.update(99L, floodEventRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldThrow_whenRegionNotFound() {
        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(floodEvent));
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());
        floodEventRequest.setRegionId(99L);

        assertThatThrownBy(() -> floodEventService.update(1L, floodEventRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(floodEventRepository.existsById(1L)).thenReturn(true);

        floodEventService.delete(1L);

        verify(floodEventRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(floodEventRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> floodEventService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(floodEventRepository, never()).deleteById(anyLong());
    }
}
