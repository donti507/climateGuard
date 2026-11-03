package com.climateguard.api.controller;

import com.climateguard.api.model.ClimateObservation;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.ObservationRepository;
import com.climateguard.api.repository.RegionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ObservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private RegionRepository regionRepository;

    private Region region;

    @BeforeEach
    void setUp() {
        region = regionRepository.save(Region.builder()
                .name("Lower Silesia")
                .country("Poland")
                .latitude(new BigDecimal("51.1079"))
                .longitude(new BigDecimal("17.0355"))
                .areaKm2(new BigDecimal("12783.00"))
                .build());
    }

    private ClimateObservation createObservation(BigDecimal temp, BigDecimal rain,
                                                  BigDecimal humidity, BigDecimal wind,
                                                  LocalDateTime observedAt) {
        return observationRepository.save(ClimateObservation.builder()
                .region(region)
                .observedAt(observedAt)
                .temperatureC(temp)
                .rainfallMm(rain)
                .humidityPct(humidity)
                .windSpeedMs(wind)
                .source("test-station")
                .build());
    }

    // ─── GET /api/v1/observations ───────────────────────────────────────────────

    @Nested
    @WithMockUser
    class GetAllObservations {

        @Test
        void shouldReturnEmptyPageWhenNoObservations() throws Exception {
            mockMvc.perform(get("/api/v1/observations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldReturnAllObservations() throws Exception {
            createObservation(new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));
            createObservation(new BigDecimal("20.1"), new BigDecimal("10.0"),
                    new BigDecimal("70.0"), new BigDecimal("8.5"),
                    LocalDateTime.of(2024, 9, 11, 12, 0));

            mockMvc.perform(get("/api/v1/observations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].regionName").value("Lower Silesia"));
        }
    }

    // ─── GET /api/v1/observations/{id} ──────────────────────────────────────────

    @Nested
    @WithMockUser
    class GetObservationById {

        @Test
        void shouldReturnObservationById() throws Exception {
            ClimateObservation obs = createObservation(
                    new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));

            mockMvc.perform(get("/api/v1/observations/{id}", obs.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(obs.getId()))
                    .andExpect(jsonPath("$.regionId").value(region.getId()))
                    .andExpect(jsonPath("$.regionName").value("Lower Silesia"))
                    .andExpect(jsonPath("$.temperatureC").value(18.5f))
                    .andExpect(jsonPath("$.rainfallMm").value(25.0f))
                    .andExpect(jsonPath("$.humidityPct").value(85.0f))
                    .andExpect(jsonPath("$.windSpeedMs").value(12.3f))
                    .andExpect(jsonPath("$.source").value("test-station"));
        }

        @Test
        void shouldReturn404WhenObservationNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/observations/{id}", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Observation not found with id: 99999"));
        }
    }

    // ─── GET /api/v1/observations/region/{regionId} ─────────────────────────────

    @Nested
    @WithMockUser
    class GetObservationsByRegion {

        @Test
        void shouldReturnObservationsForRegion() throws Exception {
            createObservation(new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));
            createObservation(new BigDecimal("20.1"), new BigDecimal("10.0"),
                    new BigDecimal("70.0"), new BigDecimal("8.5"),
                    LocalDateTime.of(2024, 9, 11, 12, 0));

            mockMvc.perform(get("/api/v1/observations/region/{regionId}", region.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        void shouldReturn404WhenRegionNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/observations/region/{regionId}", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Region not found with id: 99999"));
        }

        @Test
        void shouldFilterByDateRange() throws Exception {
            createObservation(new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));
            createObservation(new BigDecimal("20.1"), new BigDecimal("10.0"),
                    new BigDecimal("70.0"), new BigDecimal("8.5"),
                    LocalDateTime.of(2024, 9, 15, 12, 0));

            mockMvc.perform(get("/api/v1/observations/region/{regionId}", region.getId())
                            .param("from", "2024-09-11T00:00:00")
                            .param("to", "2024-09-16T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldFilterBySource() throws Exception {
            createObservation(new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));

            ClimateObservation obs2 = observationRepository.save(ClimateObservation.builder()
                    .region(region)
                    .observedAt(LocalDateTime.of(2024, 9, 11, 12, 0))
                    .temperatureC(new BigDecimal("20.0"))
                    .rainfallMm(new BigDecimal("5.0"))
                    .humidityPct(new BigDecimal("60.0"))
                    .windSpeedMs(new BigDecimal("5.0"))
                    .source("other-station")
                    .build());

            mockMvc.perform(get("/api/v1/observations/region/{regionId}", region.getId())
                            .param("source", "test-station"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    // ─── GET /api/v1/observations/region/{regionId}/stats ───────────────────────

    @Nested
    @WithMockUser
    class GetObservationStats {

        @Test
        void shouldReturnStatsWithObservations() throws Exception {
            createObservation(new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));
            createObservation(new BigDecimal("20.5"), new BigDecimal("15.0"),
                    new BigDecimal("75.0"), new BigDecimal("8.7"),
                    LocalDateTime.of(2024, 9, 11, 12, 0));

            mockMvc.perform(get("/api/v1/observations/region/{regionId}/stats", region.getId())
                            .param("from", "2024-09-01T00:00:00")
                            .param("to", "2024-09-30T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.regionId").value(region.getId()))
                    .andExpect(jsonPath("$.regionName").value("Lower Silesia"))
                    .andExpect(jsonPath("$.observationCount").value(2))
                    .andExpect(jsonPath("$.avgTemperatureC").value(19.5f))
                    .andExpect(jsonPath("$.maxTemperatureC").value(20.5f))
                    .andExpect(jsonPath("$.minTemperatureC").value(18.5f))
                    .andExpect(jsonPath("$.totalRainfallMm").value(40.0f))
                    .andExpect(jsonPath("$.avgHumidityPct").value(80.0f))
                    .andExpect(jsonPath("$.avgWindSpeedMs").value(10.5f));
        }

        @Test
        void shouldReturnZeroStatsWhenNoObservationsInPeriod() throws Exception {
            createObservation(new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));

            mockMvc.perform(get("/api/v1/observations/region/{regionId}/stats", region.getId())
                            .param("from", "2024-01-01T00:00:00")
                            .param("to", "2024-01-31T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.observationCount").value(0));
        }

        @Test
        void shouldReturn404WhenRegionNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/observations/region/{regionId}/stats", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Region not found with id: 99999"));
        }
    }

    // ─── POST /api/v1/observations ──────────────────────────────────────────────

    @Nested
    @WithMockUser(authorities = "ADMIN")
    class CreateObservation {

        @Test
        void shouldCreateObservation() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "observedAt", "2024-09-10T12:00:00",
                    "temperatureC", "18.5",
                    "rainfallMm", "25.0",
                    "humidityPct", "85.0",
                    "windSpeedMs", "12.3",
                    "source", "test-station"
            ));

            mockMvc.perform(post("/api/v1/observations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.regionId").value(region.getId()))
                    .andExpect(jsonPath("$.regionName").value("Lower Silesia"))
                    .andExpect(jsonPath("$.temperatureC").value(18.5f))
                    .andExpect(jsonPath("$.rainfallMm").value(25.0f))
                    .andExpect(jsonPath("$.humidityPct").value(85.0f))
                    .andExpect(jsonPath("$.windSpeedMs").value(12.3f))
                    .andExpect(jsonPath("$.source").value("test-station"));
        }

        @Test
        void shouldReturn400WhenRegionIdMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "observedAt", "2024-09-10T12:00:00",
                    "temperatureC", "18.5"
            ));

            mockMvc.perform(post("/api/v1/observations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void shouldReturn400WhenObservedAtMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "temperatureC", "18.5"
            ));

            mockMvc.perform(post("/api/v1/observations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void shouldReturn400WhenHumidityOutOfRange() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "observedAt", "2024-09-10T12:00:00",
                    "humidityPct", "150.0"
            ));

            mockMvc.perform(post("/api/v1/observations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn404WhenRegionNotFound() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", 99999L,
                    "observedAt", "2024-09-10T12:00:00",
                    "temperatureC", "18.5"
            ));

            mockMvc.perform(post("/api/v1/observations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Region not found with id: 99999"));
        }
    }

    // ─── DELETE /api/v1/observations/{id} ───────────────────────────────────────

    @Nested
    @WithMockUser(authorities = "ADMIN")
    class DeleteObservation {

        @Test
        void shouldDeleteObservation() throws Exception {
            ClimateObservation obs = createObservation(
                    new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));

            mockMvc.perform(delete("/api/v1/observations/{id}", obs.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/observations/{id}", obs.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenObservationNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/observations/{id}", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Observation not found with id: 99999"));
        }
    }

    // ─── Security: Authentication ───────────────────────────────────────────────

    @Nested
    class Authentication {

        @Test
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/observations"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401WhenUnauthenticatedGetById() throws Exception {
            mockMvc.perform(get("/api/v1/observations/{id}", 1))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401WhenUnauthenticatedGetStats() throws Exception {
            mockMvc.perform(get("/api/v1/observations/region/{regionId}/stats", 1))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── Security: Authorization ────────────────────────────────────────────────

    @Nested
    class Authorization {

        @Test
        @WithMockUser
        void shouldReturn403WhenNonAdminCreatesObservation() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "observedAt", "2024-09-10T12:00:00",
                    "temperatureC", "18.5"
            ));

            mockMvc.perform(post("/api/v1/observations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        void shouldReturn403WhenNonAdminDeletesObservation() throws Exception {
            ClimateObservation obs = createObservation(
                    new BigDecimal("18.5"), new BigDecimal("25.0"),
                    new BigDecimal("85.0"), new BigDecimal("12.3"),
                    LocalDateTime.of(2024, 9, 10, 12, 0));

            mockMvc.perform(delete("/api/v1/observations/{id}", obs.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
