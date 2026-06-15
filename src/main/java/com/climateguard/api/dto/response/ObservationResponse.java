package com.climateguard.api.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ObservationResponse {
    private Long id;
    private Long regionId;
    private String regionName;
    private LocalDateTime observedAt;
    private BigDecimal temperatureC;
    private BigDecimal rainfallMm;
    private BigDecimal humidityPct;
    private BigDecimal windSpeedMs;
    private String source;
    private LocalDateTime createdAt;
}
