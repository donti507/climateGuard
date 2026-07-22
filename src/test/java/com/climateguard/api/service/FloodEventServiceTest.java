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
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FloodEventServiceTest {

    @Mock
    private FloodEventRepository floodEventRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private FloodEventMapper floodEventMapper;

    @InjectMocks
    private FloodEventService floodEventService;

    private FloodEvent floodEvent1;
    private FloodEvent floodEvent2;
    private FloodEventResponse floodEventResponse1;
    private FloodEventResponse floodEventResponse2;
    private Region region1;
    private FloodEventRequest floodEventRequest;

    @BeforeEach
    void setUp() {
        region1 = Region.builder().id(1L).name("Test Region").build();

        floodEvent1 = FloodEvent.builder()
                .id(1L)
                .region(region1)
                .eventType("Flood")
                .severity("High")
                .riskScore(BigDecimal.valueOf(0.8))
                .startedAt(LocalDateTime.now().minusHours(2))
                .endedAt(LocalDateTime.now().minusHours(1))
                .description("Heavy rainfall in test region.")
                .build();

        floodEvent2 = FloodEvent.builder()
                .id(2L)
                .region(region1)
                .eventType("Storm Surge")
                .severity("Medium")
                .riskScore(BigDecimal.valueOf(0.6))
                .startedAt(LocalDateTime.now().minusHours(1))
                .endedAt(LocalDateTime.now())
                .description("Coastal flooding due to storm surge.")
                .build();

        floodEventResponse1 = FloodEventResponse.builder()
                .id(1L)
                .regionId(region1.getId())
                .eventType("Flood")
                .severity("High")
                .riskScore(BigDecimal.valueOf(0.8))
                .startedAt(floodEvent1.getStartedAt())
                .endedAt(floodEvent1.getEndedAt())
                .description("Heavy rainfall in test region.")
                .build();

        floodEventResponse2 = FloodEventResponse.builder()
                .id(2L)
                .regionId(region1.getId())
                .eventType("Storm Surge")
                .severity("Medium")
                .riskScore(BigDecimal.valueOf(0.6))
                .startedAt(floodEvent2.getStartedAt())
                .endedAt(floodEvent2.getEndedAt())
                .description("Coastal flooding due to storm surge.")
                .build();

        floodEventRequest = FloodEventRequest.builder()
                .regionId(region1.getId())
                .eventType("Flood")
                .severity("High")
                .riskScore(BigDecimal.valueOf(0.8))
                .startedAt(LocalDateTime.now().minusHours(2))
                .endedAt(LocalDateTime.now().minusHours(1))
                .description("Heavy rainfall in test region.")
                .build();
    }

    @Test
    void getAll_shouldReturnPageOfFloodEventResponses() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<FloodEvent> floodEvents = Arrays.asList(floodEvent1, floodEvent2);
        Page<FloodEvent> page = new PageImpl<>(floodEvents, pageable, floodEvents.size());

        // Mock repository call expects Double for minRisk, aligning with service method signature.
        when(floodEventRepository.findFiltered(anyString(), anyString(), any(Double.class), any(Pageable.class)))
                .thenReturn(page);
        when(floodEventMapper.toResponse(any(FloodEvent.class)))
                .thenReturn(floodEventResponse1, floodEventResponse2);

        // Service method expects Double for minRisk
        Page<FloodEventResponse> result = floodEventService.getAll("High", "Flood", 0.7, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(floodEventResponse1.getId(), result.getContent().get(0).getId());
        assertEquals(floodEventResponse2.getId(), result.getContent().get(1).getId());

        // Verify with the Double value passed to the service and expected by the mock.
        verify(floodEventRepository).findFiltered(eq("High"), eq("Flood"), eq(0.7), eq(pageable));
        verify(floodEventMapper, times(2)).toResponse(any(FloodEvent.class));
    }

    @Test
    void getById_shouldReturnFloodEventResponse() {
        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(floodEvent1));
        when(floodEventMapper.toResponse(floodEvent1)).thenReturn(floodEventResponse1);

        FloodEventResponse result = floodEventService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Flood", result.getEventType());

        verify(floodEventRepository).findById(1L);
        verify(floodEventMapper).toResponse(floodEvent1);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException() {
        when(floodEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> floodEventService.getById(99L));

        verify(floodEventRepository).findById(99L);
        verify(floodEventMapper, never()).toResponse(any(FloodEvent.class));
    }

    @Test
    void getByRegion_shouldReturnPageOfFloodEventResponses() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<FloodEvent> floodEvents = Arrays.asList(floodEvent1);
        Page<FloodEvent> page = new PageImpl<>(floodEvents, pageable, floodEvents.size());

        when(regionRepository.existsById(1L)).thenReturn(true);
        when(floodEventRepository.findByRegionId(1L, pageable)).thenReturn(page);
        when(floodEventMapper.toResponse(floodEvent1)).thenReturn(floodEventResponse1);

        Page<FloodEventResponse> result = floodEventService.getByRegion(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(floodEventResponse1.getId(), result.getContent().get(0).getId());

        verify(regionRepository).existsById(1L);
        verify(floodEventRepository).findByRegionId(1L, pageable);
        verify(floodEventMapper).toResponse(floodEvent1);
    }

    @Test
    void getByRegion_shouldThrowResourceNotFoundExceptionForRegion() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        when(regionRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> floodEventService.getByRegion(99L, pageable));

        verify(regionRepository).existsById(99L);
        verify(floodEventRepository, never()).findByRegionId(anyLong(), any(Pageable.class));
        verify(floodEventMapper, never()).toResponse(any(FloodEvent.class));
    }

    @Test
    void getHighRiskEvents_shouldReturnListOfHighRiskFloodEventResponses() {
        List<FloodEvent> highRiskEvents = Arrays.asList(floodEvent1); // floodEvent1 has riskScore 0.8
        when(floodEventRepository.findHighRiskEvents()).thenReturn(highRiskEvents);
        when(floodEventMapper.toResponse(floodEvent1)).thenReturn(floodEventResponse1);

        List<FloodEventResponse> result = floodEventService.getHighRiskEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(floodEventResponse1.getId(), result.get(0).getId());

        verify(floodEventRepository).findHighRiskEvents();
        verify(floodEventMapper).toResponse(floodEvent1);
    }

    @Test
    void create_shouldReturnCreatedFloodEventResponse() {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region1));
        when(floodEventRepository.save(any(FloodEvent.class))).thenReturn(floodEvent1);
        when(floodEventMapper.toResponse(floodEvent1)).thenReturn(floodEventResponse1);

        FloodEventResponse result = floodEventService.create(floodEventRequest);

        assertNotNull(result);
        assertEquals(floodEventResponse1.getId(), result.getId());
        assertEquals("Flood", result.getEventType());

        verify(regionRepository).findById(1L);
        verify(floodEventRepository).save(any(FloodEvent.class));
        verify(floodEventMapper).toResponse(floodEvent1);
    }

    @Test
    void create_shouldThrowResourceNotFoundExceptionForRegion() {
        FloodEventRequest invalidRequest = FloodEventRequest.builder()
                .regionId(99L) // Non-existent region
                .eventType("Flood")
                .severity("High")
                .riskScore(BigDecimal.valueOf(0.8))
                .startedAt(LocalDateTime.now().minusHours(2))
                .endedAt(LocalDateTime.now().minusHours(1))
                .description("Heavy rainfall in test region.")
                .build();

        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> floodEventService.create(invalidRequest));

        verify(regionRepository).findById(99L);
        verify(floodEventRepository, never()).save(any(FloodEvent.class));
        verify(floodEventMapper, never()).toResponse(any(FloodEvent.class));
    }

    @Test
    void update_shouldReturnUpdatedFloodEventResponse() {
        FloodEvent updatedEvent = FloodEvent.builder()
                .id(1L)
                .region(region1)
                .eventType("Flood Update")
                .severity("Medium")
                .riskScore(BigDecimal.valueOf(0.5))
                .startedAt(LocalDateTime.now().minusHours(3))
                .endedAt(LocalDateTime.now().minusHours(2))
                .description("Updated description.")
                .build();
        FloodEventResponse updatedResponse = FloodEventResponse.builder()
                .id(1L)
                .regionId(region1.getId())
                .eventType("Flood Update")
                .severity("Medium")
                .riskScore(BigDecimal.valueOf(0.5))
                .startedAt(updatedEvent.getStartedAt())
                .endedAt(updatedEvent.getEndedAt())
                .description("Updated description.")
                .build();

        FloodEventRequest updateRequest = FloodEventRequest.builder()
                .regionId(region1.getId())
                .eventType("Flood Update")
                .severity("Medium")
                .riskScore(BigDecimal.valueOf(0.5))
                .startedAt(LocalDateTime.now().minusHours(3))
                .endedAt(LocalDateTime.now().minusHours(2))
                .description("Updated description.")
                .build();

        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(floodEvent1)); // Original event
        when(regionRepository.findById(region1.getId())).thenReturn(Optional.of(region1));
        when(floodEventRepository.save(any(FloodEvent.class))).thenReturn(updatedEvent); // Saved updated event
        when(floodEventMapper.toResponse(updatedEvent)).thenReturn(updatedResponse);

        FloodEventResponse result = floodEventService.update(1L, updateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Flood Update", result.getEventType());
        assertEquals("Medium", result.getSeverity());

        verify(floodEventRepository).findById(1L);
        verify(regionRepository).findById(region1.getId());
        verify(floodEventRepository).save(any(FloodEvent.class));
        verify(floodEventMapper).toResponse(updatedEvent);
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionForEvent() {
        FloodEventRequest updateRequest = FloodEventRequest.builder()
                .regionId(region1.getId())
                .eventType("Flood")
                .severity("High")
                .riskScore(BigDecimal.valueOf(0.8))
                .build(); // Minimal valid request, content doesn't matter here for this test case
        when(floodEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> floodEventService.update(99L, updateRequest));

        verify(floodEventRepository).findById(99L);
        verify(regionRepository, never()).findById(anyLong());
        verify(floodEventRepository, never()).save(any(FloodEvent.class));
        verify(floodEventMapper, never()).toResponse(any(FloodEvent.class));
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionForRegion() {
        FloodEventRequest updateRequest = FloodEventRequest.builder()
                .regionId(99L) // Non-existent region
                .eventType("Flood")
                .severity("High")
                .riskScore(BigDecimal.valueOf(0.8))
                .startedAt(LocalDateTime.now().minusHours(2))
                .endedAt(LocalDateTime.now().minusHours(1))
                .description("Heavy rainfall in test region.")
                .build();

        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(floodEvent1));
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> floodEventService.update(1L, updateRequest));

        verify(floodEventRepository).findById(1L);
        verify(regionRepository).findById(99L);
        verify(floodEventRepository, never()).save(any(FloodEvent.class));
        verify(floodEventMapper, never()).toResponse(any(FloodEvent.class));
    }

    @Test
    void delete_shouldSucceed() {
        when(floodEventRepository.existsById(1L)).thenReturn(true);
        doNothing().when(floodEventRepository).deleteById(1L);

        floodEventService.delete(1L);

        verify(floodEventRepository).existsById(1L);
        verify(floodEventRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException() {
        when(floodEventRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> floodEventService.delete(99L));

        verify(floodEventRepository).existsById(99L);
        verify(floodEventRepository, never()).deleteById(anyLong());
    }
}
