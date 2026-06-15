package com.climateguard.api.service;

import com.climateguard.api.dto.request.RegionRequest;
import com.climateguard.api.dto.response.RegionResponse;
import com.climateguard.api.exception.ResourceNotFoundException;
import com.climateguard.api.mapper.RegionMapper;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock private RegionRepository regionRepository;
    @Mock private RegionMapper regionMapper;
    @InjectMocks private RegionService regionService;

    private Region region;
    private RegionResponse regionResponse;
    private RegionRequest regionRequest;

    @BeforeEach
    void setUp() {
        region = Region.builder()
                .id(1L).name("Siechnice").country("Poland")
                .latitude(new BigDecimal("51.034700"))
                .longitude(new BigDecimal("17.147200"))
                .build();
        regionResponse = new RegionResponse();
        regionResponse.setId(1L);
        regionResponse.setName("Siechnice");
        regionRequest = new RegionRequest();
        regionRequest.setName("Siechnice");
        regionRequest.setCountry("Poland");
        regionRequest.setLatitude(new BigDecimal("51.034700"));
        regionRequest.setLongitude(new BigDecimal("17.147200"));
    }

    @Test
    void getRegionById_shouldReturnRegion_whenExists() {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(regionMapper.toResponse(region)).thenReturn(regionResponse);
        RegionResponse result = regionService.getRegionById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Siechnice");
    }

    @Test
    void getRegionById_shouldThrow_whenNotFound() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> regionService.getRegionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createRegion_shouldSaveAndReturn() {
        when(regionMapper.toEntity(regionRequest)).thenReturn(region);
        when(regionRepository.save(region)).thenReturn(region);
        when(regionMapper.toResponse(region)).thenReturn(regionResponse);
        RegionResponse result = regionService.createRegion(regionRequest);
        assertThat(result).isNotNull();
        verify(regionRepository, times(1)).save(any(Region.class));
    }

    @Test
    void deleteRegion_shouldDelete_whenExists() {
        when(regionRepository.existsById(1L)).thenReturn(true);
        regionService.deleteRegion(1L);
        verify(regionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRegion_shouldThrow_whenNotFound() {
        when(regionRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> regionService.deleteRegion(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
