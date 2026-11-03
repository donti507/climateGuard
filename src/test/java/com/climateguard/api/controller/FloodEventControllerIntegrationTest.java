package com.climateguard.api.controller;

import com.climateguard.api.model.FloodEvent;
import com.climateguard.api.model.Region;
import com.climateguard.api.repository.FloodEventRepository;
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
class FloodEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FloodEventRepository floodEventRepository;

    @Autowired
    private RegionRepository regionRepository;

    private Region region;

    @BeforeEach
    void setUp() {
        region = regionRepository.save(Region.builder()
                .name("Silesia")
                .country("Poland")
                .latitude(new BigDecimal("50.2649"))
                .longitude(new BigDecimal("19.0238"))
                .areaKm2(new BigDecimal("14718.00"))
                .build());
    }

    private FloodEvent createFloodEvent(String eventType, String severity, BigDecimal riskScore) {
        return floodEventRepository.save(FloodEvent.builder()
                .region(region)
                .eventType(eventType)
                .severity(severity)
                .riskScore(riskScore)
                .startedAt(LocalDateTime.of(2024, 9, 10, 0, 0))
                .endedAt(LocalDateTime.of(2024, 9, 12, 0, 0))
                .description("Test flood event")
                .build());
    }

    private String floodEventRequestJson(Long regionId, String eventType, String severity,
                                         String riskScore, String startedAt, String endedAt,
                                         String description) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "regionId", regionId,
                "eventType", eventType,
                "severity", severity,
                "riskScore", riskScore,
                "startedAt", startedAt,
                "endedAt", endedAt,
                "description", description
        ));
    }

    // ─── GET /api/v1/flood-events ────────────────────────────────────────────────

    @Nested
    @WithMockUser
    class GetAllFloodEvents {

        @Test
        void shouldReturnEmptyPageWhenNoEvents() throws Exception {
            mockMvc.perform(get("/api/v1/flood-events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldReturnAllEvents() throws Exception {
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));
            createFloodEvent("PREDICTED", "MODERATE", new BigDecimal("0.50"));

            mockMvc.perform(get("/api/v1/flood-events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].eventType").isString())
                    .andExpect(jsonPath("$.content[0].regionName").value("Silesia"));
        }

        @Test
        void shouldFilterBySeverity() throws Exception {
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));
            createFloodEvent("HISTORICAL", "LOW", new BigDecimal("0.20"));

            mockMvc.perform(get("/api/v1/flood-events").param("severity", "HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].severity").value("HIGH"));
        }

        @Test
        void shouldFilterByEventType() throws Exception {
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));
            createFloodEvent("PREDICTED", "HIGH", new BigDecimal("0.90"));

            mockMvc.perform(get("/api/v1/flood-events").param("type", "PREDICTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].eventType").value("PREDICTED"));
        }

        @Test
        void shouldFilterByMinRisk() throws Exception {
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));
            createFloodEvent("HISTORICAL", "LOW", new BigDecimal("0.30"));

            mockMvc.perform(get("/api/v1/flood-events").param("minRisk", "0.5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].riskScore").value(0.85f));
        }

        @Test
        void shouldCombineFilters() throws Exception {
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));
            createFloodEvent("PREDICTED", "HIGH", new BigDecimal("0.90"));
            createFloodEvent("HISTORICAL", "LOW", new BigDecimal("0.30"));

            mockMvc.perform(get("/api/v1/flood-events")
                            .param("severity", "HIGH")
                            .param("type", "HISTORICAL")
                            .param("minRisk", "0.5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].eventType").value("HISTORICAL"))
                    .andExpect(jsonPath("$.content[0].severity").value("HIGH"));
        }
    }

    // ─── GET /api/v1/flood-events/{id} ──────────────────────────────────────────

    @Nested
    @WithMockUser
    class GetFloodEventById {

        @Test
        void shouldReturnEventById() throws Exception {
            FloodEvent event = createFloodEvent("HISTORICAL", "CRITICAL", new BigDecimal("0.95"));

            mockMvc.perform(get("/api/v1/flood-events/{id}", event.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(event.getId()))
                    .andExpect(jsonPath("$.eventType").value("HISTORICAL"))
                    .andExpect(jsonPath("$.severity").value("CRITICAL"))
                    .andExpect(jsonPath("$.regionId").value(region.getId()))
                    .andExpect(jsonPath("$.regionName").value("Silesia"));
        }

        @Test
        void shouldReturn404WhenEventNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/flood-events/{id}", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Flood event not found with id: 99999"));
        }
    }

    // ─── GET /api/v1/flood-events/region/{regionId} ─────────────────────────────

    @Nested
    @WithMockUser
    class GetFloodEventsByRegion {

        @Test
        void shouldReturnEventsForRegion() throws Exception {
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));
            createFloodEvent("PREDICTED", "MODERATE", new BigDecimal("0.50"));

            mockMvc.perform(get("/api/v1/flood-events/region/{regionId}", region.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        void shouldReturn404WhenRegionNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/flood-events/region/{regionId}", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Region not found with id: 99999"));
        }
    }

    // ─── GET /api/v1/flood-events/high-risk ─────────────────────────────────────

    @Nested
    @WithMockUser
    class GetHighRiskEvents {

        @Test
        void shouldReturnOnlyHighRiskEvents() throws Exception {
            createFloodEvent("HISTORICAL", "CRITICAL", new BigDecimal("0.95"));
            createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.80"));
            createFloodEvent("HISTORICAL", "LOW", new BigDecimal("0.30"));

            mockMvc.perform(get("/api/v1/flood-events/high-risk"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].riskScore", greaterThan(0.7f)))
                    .andExpect(jsonPath("$[1].riskScore", greaterThan(0.7f)));
        }

        @Test
        void shouldReturnEmptyListWhenNoHighRiskEvents() throws Exception {
            createFloodEvent("HISTORICAL", "LOW", new BigDecimal("0.30"));

            mockMvc.perform(get("/api/v1/flood-events/high-risk"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ─── POST /api/v1/flood-events ──────────────────────────────────────────────

    @Nested
    @WithMockUser(authorities = "ADMIN")
    class CreateFloodEvent {

        @Test
        void shouldCreateFloodEvent() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "HISTORICAL",
                    "severity", "HIGH",
                    "riskScore", "0.85",
                    "startedAt", "2024-09-10T00:00:00",
                    "endedAt", "2024-09-12T00:00:00",
                    "description", "Major flooding in Silesia"
            ));

            mockMvc.perform(post("/api/v1/flood-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.eventType").value("HISTORICAL"))
                    .andExpect(jsonPath("$.severity").value("HIGH"))
                    .andExpect(jsonPath("$.regionId").value(region.getId()))
                    .andExpect(jsonPath("$.regionName").value("Silesia"))
                    .andExpect(jsonPath("$.description").value("Major flooding in Silesia"));
        }

        @Test
        void shouldReturn400WhenEventTypeInvalid() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "INVALID",
                    "severity", "HIGH",
                    "riskScore", "0.85"
            ));

            mockMvc.perform(post("/api/v1/flood-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void shouldReturn400WhenSeverityInvalid() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "HISTORICAL",
                    "severity", "INVALID",
                    "riskScore", "0.85"
            ));

            mockMvc.perform(post("/api/v1/flood-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void shouldReturn400WhenRiskScoreOutOfRange() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "HISTORICAL",
                    "severity", "HIGH",
                    "riskScore", "1.5"
            ));

            mockMvc.perform(post("/api/v1/flood-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn404WhenRegionNotFound() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", 99999L,
                    "eventType", "HISTORICAL",
                    "severity", "HIGH",
                    "riskScore", "0.85"
            ));

            mockMvc.perform(post("/api/v1/flood-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Region not found with id: 99999"));
        }
    }

    // ─── PUT /api/v1/flood-events/{id} ──────────────────────────────────────────

    @Nested
    @WithMockUser(authorities = "ADMIN")
    class UpdateFloodEvent {

        @Test
        void shouldUpdateFloodEvent() throws Exception {
            FloodEvent event = createFloodEvent("HISTORICAL", "LOW", new BigDecimal("0.20"));

            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "PREDICTED",
                    "severity", "CRITICAL",
                    "riskScore", "0.95",
                    "startedAt", "2024-10-01T00:00:00",
                    "endedAt", "2024-10-03T00:00:00",
                    "description", "Updated description"
            ));

            mockMvc.perform(put("/api/v1/flood-events/{id}", event.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(event.getId()))
                    .andExpect(jsonPath("$.eventType").value("PREDICTED"))
                    .andExpect(jsonPath("$.severity").value("CRITICAL"))
                    .andExpect(jsonPath("$.description").value("Updated description"));
        }

        @Test
        void shouldReturn404WhenEventNotFound() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "HISTORICAL",
                    "severity", "HIGH",
                    "riskScore", "0.85"
            ));

            mockMvc.perform(put("/api/v1/flood-events/{id}", 99999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Flood event not found with id: 99999"));
        }
    }

    // ─── DELETE /api/v1/flood-events/{id} ───────────────────────────────────────

    @Nested
    @WithMockUser(authorities = "ADMIN")
    class DeleteFloodEvent {

        @Test
        void shouldDeleteFloodEvent() throws Exception {
            FloodEvent event = createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));

            mockMvc.perform(delete("/api/v1/flood-events/{id}", event.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/flood-events/{id}", event.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenEventNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/flood-events/{id}", 99999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Flood event not found with id: 99999"));
        }
    }

    // ─── Security: Authentication ───────────────────────────────────────────────

    @Nested
    class Authentication {

        @Test
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/flood-events"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401WhenUnauthenticatedGetById() throws Exception {
            mockMvc.perform(get("/api/v1/flood-events/{id}", 1))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── Security: Authorization ────────────────────────────────────────────────

    @Nested
    class Authorization {

        @Test
        @WithMockUser
        void shouldReturn403WhenNonAdminCreatesEvent() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "HISTORICAL",
                    "severity", "HIGH",
                    "riskScore", "0.85"
            ));

            mockMvc.perform(post("/api/v1/flood-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        void shouldReturn403WhenNonAdminUpdatesEvent() throws Exception {
            FloodEvent event = createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));

            String body = objectMapper.writeValueAsString(Map.of(
                    "regionId", region.getId(),
                    "eventType", "PREDICTED",
                    "severity", "LOW",
                    "riskScore", "0.30"
            ));

            mockMvc.perform(put("/api/v1/flood-events/{id}", event.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        void shouldReturn403WhenNonAdminDeletesEvent() throws Exception {
            FloodEvent event = createFloodEvent("HISTORICAL", "HIGH", new BigDecimal("0.85"));

            mockMvc.perform(delete("/api/v1/flood-events/{id}", event.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
