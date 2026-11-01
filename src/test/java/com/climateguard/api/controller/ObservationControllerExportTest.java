package com.climateguard.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.climateguard.api.dto.response.ObservationResponse;
import com.climateguard.api.service.ObservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ObservationControllerExportTest {

    @Mock
    private ObservationService observationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObservationController controller = new ObservationController(observationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void exportCsvReturnsCsvBody() throws Exception {
        ObservationResponse observation = new ObservationResponse();
        observation.setId(1L);
        observation.setRegionId(10L);
        observation.setRegionName("Mazowieckie");
        observation.setObservedAt(LocalDateTime.of(2024, 9, 1, 12, 0, 30));
        observation.setTemperatureC(BigDecimal.valueOf(18.5));
        observation.setRainfallMm(BigDecimal.valueOf(12.0));
        observation.setHumidityPct(BigDecimal.valueOf(80.0));
        observation.setWindSpeedMs(BigDecimal.valueOf(3.2));
        observation.setSource("station");
        observation.setCreatedAt(LocalDateTime.of(2024, 9, 1, 12, 5, 0));

        when(observationService.getExportByRegion(eq(10L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(observation));

        String expected = "id,regionId,regionName,observedAt,temperatureC,rainfallMm,humidityPct,windSpeedMs,source,createdAt\n" +
                "1,10,Mazowieckie,2024-09-01T12:00:30,18.5,12.0,80.0,3.2,station,2024-09-01T12:05:00\n";

        mockMvc.perform(get("/api/v1/observations/region/10/export").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(expected));
    }

    @Test
    void exportCsvEscapesSpecialCharacters() throws Exception {
        ObservationResponse observation = new ObservationResponse();
        observation.setId(1L);
        observation.setRegionId(10L);
        observation.setRegionName("Quote,\"Name");
        observation.setSource("a,b");

        when(observationService.getExportByRegion(eq(10L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(observation));

        mockMvc.perform(get("/api/v1/observations/region/10/export").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("\"Quote,\"\"Name\"")))
                .andExpect(content().string(containsString("\"a,b\"")));
    }

    @Test
    void exportJsonReturnsJsonArray() throws Exception {
        ObservationResponse observation = new ObservationResponse();
        observation.setId(1L);
        observation.setRegionId(10L);
        observation.setRegionName("Mazowieckie");
        observation.setObservedAt(LocalDateTime.of(2024, 9, 1, 12, 0, 30));
        observation.setTemperatureC(BigDecimal.valueOf(18.5));
        observation.setRainfallMm(BigDecimal.valueOf(12.0));
        observation.setHumidityPct(BigDecimal.valueOf(80.0));
        observation.setWindSpeedMs(BigDecimal.valueOf(3.2));
        observation.setSource("station");
        observation.setCreatedAt(LocalDateTime.of(2024, 9, 1, 12, 5, 0));

        when(observationService.getExportByRegion(eq(10L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(observation));

        mockMvc.perform(get("/api/v1/observations/region/10/export").param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].regionId").value(10))
                .andExpect(jsonPath("$[0].regionName").value("Mazowieckie"))
                .andExpect(jsonPath("$[0].observedAt").value("2024-09-01T12:00:30"))
                .andExpect(jsonPath("$[0].temperatureC").value(18.5))
                .andExpect(jsonPath("$[0].rainfallMm").value(12.0))
                .andExpect(jsonPath("$[0].humidityPct").value(80.0))
                .andExpect(jsonPath("$[0].windSpeedMs").value(3.2))
                .andExpect(jsonPath("$[0].source").value("station"))
                .andExpect(jsonPath("$[0].createdAt").value("2024-09-01T12:05:00"));
    }

    @Test
    void exportJsonPassesFiltersToService() throws Exception {
        LocalDateTime from = LocalDateTime.of(2024, 9, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 9, 30, 23, 59, 59);

        when(observationService.getExportByRegion(eq(10L), eq(from), eq(to), eq("station")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/observations/region/10/export")
                        .param("format", "json")
                        .param("from", "2024-09-01T00:00:00")
                        .param("to", "2024-09-30T23:59:59")
                        .param("source", "station"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());

        verify(observationService).getExportByRegion(10L, from, to, "station");
    }

    @Test
    void exportDefaultsToCsvWhenFormatIsMissing() throws Exception {
        when(observationService.getExportByRegion(eq(10L), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        String expected = "id,regionId,regionName,observedAt,temperatureC,rainfallMm,humidityPct,windSpeedMs,source,createdAt\n";

        mockMvc.perform(get("/api/v1/observations/region/10/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(expected));

        verify(observationService).getExportByRegion(10L, null, null, null);
    }

    @Test
    void exportAcceptsCaseInsensitiveFormat() throws Exception {
        when(observationService.getExportByRegion(eq(10L), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/observations/region/10/export").param("format", "JSON"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(observationService).getExportByRegion(10L, null, null, null);
    }

    @Test
    void exportReturnsBadRequestForUnsupportedFormat() throws Exception {
        mockMvc.perform(get("/api/v1/observations/region/10/export").param("format", "xml"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported export format: xml"));

        verifyNoInteractions(observationService);
    }
}
