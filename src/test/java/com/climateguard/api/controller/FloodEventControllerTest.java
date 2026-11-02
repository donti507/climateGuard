package com.climateguard.api.controller;

import com.climateguard.api.dto.request.FloodEventRequest;
import com.climateguard.api.dto.response.FloodEventResponse;
import com.climateguard.api.service.FloodEventService;
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

@WebMvcTest(FloodEventController.class)
class FloodEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FloodEventService floodEventService;

    @Test
    @DisplayName("GET /api/v1/flood-events returns paginated list")
    @WithMockUser(roles = "USER")
    void getAllFloodEvents() throws Exception {
        FloodEventResponse event = new FloodEventResponse(
                1L, 10L, "Riverside", "HIGH", "FLASH_FLOOD",
                0.85, LocalDateTime.of(2026, 7, 15, 8, 0),
                LocalDateTime.of(2026, 7, 15, 14, 0),
                "Severe flash flood after heavy rainfall"
        );
        Pageable pageable = PageRequest.of(0, 20);
        when(floodEventService.getAll(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));

        mockMvc.perform(get("/api/v1/flood-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.content[0].riskScore").value(0.85))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/flood-events/{id} returns event by id")
    @WithMockUser(roles = "USER")
    void getFloodEventById() throws Exception {
        FloodEventResponse event = new FloodEventResponse(
                1L, 10L, "Riverside", "HIGH", "FLASH_FLOOD",
                0.85, LocalDateTime.of(2026, 7, 15, 8, 0),
                LocalDateTime.of(2026, 7, 15, 14, 0),
                "Severe flash flood after heavy rainfall"
        );
        when(floodEventService.getById(1L)).thenReturn(event);

        mockMvc.perform(get("/api/v1/flood-events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.regionName").value("Riverside"))
                .andExpect(jsonPath("$.type").value("FLASH_FLOOD"));
    }

    @Test
    @DisplayName("GET /api/v1/flood-events/high-risk returns list of high risk events")
    @WithMockUser(roles = "USER")
    void getHighRiskEvents() throws Exception {
        FloodEventResponse event = new FloodEventResponse(
                1L, 10L, "Riverside", "CRITICAL", "RIVERINE",
                0.92, LocalDateTime.of(2026, 8, 1, 3, 0),
                LocalDateTime.of(2026, 8, 2, 18, 0),
                "River overflow due to sustained rainfall"
        );
        when(floodEventService.getHighRiskEvents()).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/flood-events/high-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].riskScore").value(0.92));
    }

    @Test
    @DisplayName("POST /api/v1/flood-events creates event and returns 201")
    @WithMockUser(roles = "ADMIN")
    void createFloodEvent() throws Exception {
        FloodEventResponse created = new FloodEventResponse(
                3L, 12L, "Coastal Bay", "MODERATE", "COASTAL",
                0.65, LocalDateTime.of(2026, 9, 10, 6, 0),
                LocalDateTime.of(2026, 9, 10, 12, 0),
                "Coastal flooding from storm surge"
        );
        when(floodEventService.create(any(FloodEventRequest.class))).thenReturn(created);

        FloodEventRequest request = new FloodEventRequest();
        request.setRegionId(12L);
        request.setSeverity("MODERATE");
        request.setType("COASTAL");
        request.setRiskScore(0.65);
        request.setStartedAt(LocalDateTime.of(2026, 9, 10, 6, 0));
        request.setEndedAt(LocalDateTime.of(2026, 9, 10, 12, 0));
        request.setDescription("Coastal flooding from storm surge");

        mockMvc.perform(post("/api/v1/flood-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.severity").value("MODERATE"));
    }

    @Test
    @DisplayName("PUT /api/v1/flood-events/{id} updates event")
    @WithMockUser(roles = "ADMIN")
    void updateFloodEvent() throws Exception {
        FloodEventResponse updated = new FloodEventResponse(
                1L, 10L, "Riverside", "CRITICAL", "FLASH_FLOOD",
                0.95, LocalDateTime.of(2026, 7, 15, 8, 0),
                LocalDateTime.of(2026, 7, 15, 16, 0),
                "Escalated to critical"
        );
        when(floodEventService.update(eq(1L), any(FloodEventRequest.class))).thenReturn(updated);

        FloodEventRequest request = new FloodEventRequest();
        request.setRegionId(10L);
        request.setSeverity("CRITICAL");
        request.setType("FLASH_FLOOD");
        request.setRiskScore(0.95);
        request.setStartedAt(LocalDateTime.of(2026, 7, 15, 8, 0));
        request.setEndedAt(LocalDateTime.of(2026, 7, 15, 16, 0));
        request.setDescription("Escalated to critical");

        mockMvc.perform(put("/api/v1/flood-events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.riskScore").value(0.95));
    }

    @Test
    @DisplayName("DELETE /api/v1/flood-events/{id} returns 204")
    @WithMockUser(roles = "ADMIN")
    void deleteFloodEvent() throws Exception {
        mockMvc.perform(delete("/api/v1/flood-events/1"))
                .andExpect(status().isNoContent());
    }
}
