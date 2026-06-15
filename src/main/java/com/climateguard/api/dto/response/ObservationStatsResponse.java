package com.climateguard.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObservationStatsResponse {
    private Long regionId;
    private String regionName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Double avgTemperatureC;
    private Double maxTemperatureC;
    private Double minTemperatureC;
    private Double totalRainfallMm;
    private Double avgHumidityPct;
    private Double avgWindSpeedMs;
    private Long observationCount;
}
