package com.climateguard.api.controller;

import com.climateguard.api.dto.request.RegionRequest;
import com.climateguard.api.dto.response.RegionResponse;
import com.climateguard.api.service.RegionService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionController.class)
class RegionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegionService regionService;

    @Test
    @DisplayName("GET /api/v1/regions returns paginated list")
    @WithMockUser(roles = "USER")
    void getAllRegions() throws Exception {
        RegionResponse region = new RegionResponse(10L, "Riverside", "USA", 40.7128, -74.0060);
        Pageable pageable = PageRequest.of(0, 20);
        when(regionService.getAllRegions(eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(region), pageable, 1));

        mockMvc.perform(get("/api/v1/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].name").value("Riverside"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/regions/{id} returns region by id")
    @WithMockUser(roles = "USER")
    void getRegionById() throws Exception {
        RegionResponse region = new RegionResponse(10L, "Riverside", "USA", 40.7128, -74.0060);
        when(regionService.getRegionById(10L)).thenReturn(region);

        mockMvc.perform(get("/api/v1/regions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Riverside"))
                .andExpect(jsonPath("$.country").value("USA"));
    }

    @Test
    @DisplayName("POST /api/v1/regions creates a region and returns 201")
    @WithMockUser(roles = "ADMIN")
    void createRegion() throws Exception {
        RegionResponse created = new RegionResponse(12L, "Coastal Bay", "USA", 25.7617, -80.1918);
        when(regionService.createRegion(any(RegionRequest.class))).thenReturn(created);

        RegionRequest request = new RegionRequest();
        request.setName("Coastal Bay");
        request.setCountry("USA");
        request.setLatitude(25.7617);
        request.setLongitude(-80.1918);

        mockMvc.perform(post("/api/v1/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.name").value("Coastal Bay"));
    }

    @Test
    @DisplayName("PUT /api/v1/regions/{id} updates a region")
    @WithMockUser(roles = "ADMIN")
    void updateRegion() throws Exception {
        RegionResponse updated = new RegionResponse(10L, "Riverside North", "USA", 40.7580, -73.9855);
        when(regionService.updateRegion(eq(10L), any(RegionRequest.class))).thenReturn(updated);

        RegionRequest request = new RegionRequest();
        request.setName("Riverside North");
        request.setCountry("USA");
        request.setLatitude(40.7580);
        request.setLongitude(-73.9855);

        mockMvc.perform(put("/api/v1/regions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Riverside North"));
    }

    @Test
    @DisplayName("DELETE /api/v1/regions/{id} returns 204")
    @WithMockUser(roles = "ADMIN")
    void deleteRegion() throws Exception {
        mockMvc.perform(delete("/api/v1/regions/10"))
                .andExpect(status().isNoContent());
    }
}
