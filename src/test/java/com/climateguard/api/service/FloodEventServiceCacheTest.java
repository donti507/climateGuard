package com.climateguard.api.service;

import com.climateguard.api.dto.request.FloodEventRequest;
import com.climateguard.api.dto.response.FloodEventResponse;
import com.climateguard.api.mapper.FloodEventMapper;
import com.climateguard.api.model.FloodEvent;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.FloodEventRepository;
import com.climateguard.api.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = FloodEventServiceCacheTest.TestConfig.class)
class FloodEventServiceCacheTest {

    private static final String CACHE_NAME = "highRiskFloodEvents";

    @Autowired
    private FloodEventService floodEventService;

    @Autowired
    private FloodEventRepository floodEventRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private FloodEventMapper floodEventMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CACHE_NAME).clear();
        reset(floodEventRepository, regionRepository, floodEventMapper);
    }

    @Test
    void getHighRiskEventsShouldBeCached() {
        FloodEvent event = mock(FloodEvent.class);
        FloodEventResponse response = mock(FloodEventResponse.class);

        when(floodEventRepository.findHighRiskEvents()).thenReturn(List.of(event));
        when(floodEventMapper.toResponse(event)).thenReturn(response);

        List<FloodEventResponse> first = floodEventService.getHighRiskEvents();
        List<FloodEventResponse> second = floodEventService.getHighRiskEvents();

        assertEquals(List.of(response), first);
        assertEquals(first, second);

        verify(floodEventRepository, times(1)).findHighRiskEvents();
        verify(floodEventMapper, times(1)).toResponse(event);
    }

    @Test
    void createShouldEvictHighRiskCache() {
        when(floodEventRepository.findHighRiskEvents()).thenReturn(List.of());

        Region region = mock(Region.class);
        FloodEventRequest request = new FloodEventRequest();
        request.setRegionId(1L);

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        FloodEvent saved = mock(FloodEvent.class);
        when(floodEventRepository.save(any(FloodEvent.class))).thenReturn(saved);

        FloodEventResponse response = mock(FloodEventResponse.class);
        when(floodEventMapper.toResponse(saved)).thenReturn(response);

        floodEventService.getHighRiskEvents();
        floodEventService.create(request);
        floodEventService.getHighRiskEvents();

        verify(floodEventRepository, times(2)).findHighRiskEvents();
    }

    @Test
    void updateShouldEvictHighRiskCache() {
        when(floodEventRepository.findHighRiskEvents()).thenReturn(List.of());

        Region region = mock(Region.class);
        FloodEvent existing = mock(FloodEvent.class);

        FloodEventRequest request = new FloodEventRequest();
        request.setRegionId(1L);

        when(floodEventRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(floodEventRepository.save(any(FloodEvent.class))).thenReturn(existing);

        FloodEventResponse response = mock(FloodEventResponse.class);
        when(floodEventMapper.toResponse(existing)).thenReturn(response);

        floodEventService.getHighRiskEvents();
        floodEventService.update(1L, request);
        floodEventService.getHighRiskEvents();

        verify(floodEventRepository, times(2)).findHighRiskEvents();
    }

    @Test
    void deleteShouldEvictHighRiskCache() {
        when(floodEventRepository.findHighRiskEvents()).thenReturn(List.of());
        when(floodEventRepository.existsById(1L)).thenReturn(true);

        floodEventService.getHighRiskEvents();
        floodEventService.delete(1L);
        floodEventService.getHighRiskEvents();

        verify(floodEventRepository, times(2)).findHighRiskEvents();
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CACHE_NAME);
        }

        @Bean
        FloodEventRepository floodEventRepository() {
            return mock(FloodEventRepository.class);
        }

        @Bean
        RegionRepository regionRepository() {
            return mock(RegionRepository.class);
        }

        @Bean
        FloodEventMapper floodEventMapper() {
            return mock(FloodEventMapper.class);
        }

        @Bean
        FloodEventService floodEventService(FloodEventRepository floodEventRepository,
                                           RegionRepository regionRepository,
                                           FloodEventMapper floodEventMapper) {
            return new FloodEventService(floodEventRepository, regionRepository, floodEventMapper);
        }
    }
}
