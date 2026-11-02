package com.climateguard.api.controller;

import com.climateguard.api.dto.request.ObservationRequest;
import com.climateguard.api.dto.response.ObservationResponse;
import com.climateguard.api.dto.response.ObservationStatsResponse;
import com.climateguard.api.service.ObservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ObservationController.class)
class ObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ObservationService observationService;

    @Test
    @DisplayName("GET /api/v1/observations returns paginated list")
    @WithMockUser(roles = "USER")
    void getAllObservations() throws Exception {
        ObservationResponse obs = new ObservationResponse(
                1L, 10L, 22.5, 15.2, 78, 12.3,
                LocalDateTime.of(2026, 10, 1, 6, 0), "METEO_STATION_01"
        );
        Pageable pageable = PageRequest.of(0, 20);
        when(observationService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(obs), pageable, 1));

        mockMvc.perform(get("/api/v1/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].temperature").value(22.5))
                .andExpect(jsonPath("$.content[0].rainfall").value(15.2))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/observations/{id} returns observation by id")
    @WithMockUser(roles = "USER")
    void getObservationById() throws Exception {
        ObservationResponse obs = new ObservationResponse(
                1L, 10L, 22.5, 15.2, 78, 12.3,
                LocalDateTime.of(2026, 10, 1, 6, 0), "METEO_STATION_01"
        );
        when(observationService.getById(1L)).thenReturn(obs);

        mockMvc.perform(get("/api/v1/observations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.regionId").value(10))
                .andExpect(jsonPath("$.source").value("METEO_STATION_01"));
    }

    @Test
    @DisplayName("GET /api/v1/observations/region/{regionId} returns paginated observations")
    @WithMockUser(roles = "USER")
    void getObservationsByRegion() throws Exception {
        ObservationResponse obs = new ObservationResponse(
                1L, 10L, 22.5, 15.2, 78, 12.3,
                LocalDateTime.of(2026, 10, 1, 6, 0), "METEO_STATION_01"
        );
        Pageable pageable = PageRequest.of(0, 20);
        when(observationService.getByRegion(eq(10L), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(obs), pageable, 1));

        mockMvc.perform(get("/api/v1/observations/region/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].regionId").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/observations/region/{regionId}/stats returns stats")
    @WithMockUser(roles = "USER")
    void getObservationStats() throws Exception {
        ObservationStatsResponse stats = new ObservationStatsResponse(
                21.3, 145.6, 35.2, 8.1, 42
        );
        when(observationService.getStatsForRegion(eq(10L), eq(null), eq(null))).thenReturn(stats);

        mockMvc.perform(get("/api/v1/observations/region/10/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgTemperature").value(21.3))
                .andExpect(jsonPath("$.totalRainfall").value(145.6))
                .andExpect(jsonPath("$.maxTemperature").value(35.2))
                .andExpect(jsonPath("$.minTemperature").value(8.1))
                .andExpect(jsonPath("$.observationCount").value(42));
    }

    @Test
    @DisplayName("POST /api/v1/observations creates observation and returns 201")
    @WithMockUser(roles = "ADMIN")
    void createObservation() throws Exception {
        ObservationResponse created = new ObservationResponse(
                3L, 11L, 18.4, 5.0, 72, 10.1,
                LocalDateTime.of(2026, 10, 15, 6, 0), "METEO_STATION_02"
        );
        when(observationService.create(any(ObservationRequest.class))).thenReturn(created);

        ObservationRequest request = new ObservationRequest();
        request.setRegionId(11L);
        request.setTemperature(18.4);
        request.setRainfall(5.0);
        request.setHumidity(72);
        request.setWindSpeed(10.1);
        request.setObservedAt(LocalDateTime.of(2026, 10, 15, 6, 0));
        request.setSource("METEO_STATION_02");

        mockMvc.perform(post("/api/v1/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.temperature").value(18.4));
    }

    @Test
    @DisplayName("DELETE /api/v1/observations/{id} returns 204")
    @WithMockUser(roles = "ADMIN")
    void deleteObservation() throws Exception {
        mockMvc.perform(delete("/api/v1/observations/1"))
                .andExpect(status().isNoContent());
    }
}
