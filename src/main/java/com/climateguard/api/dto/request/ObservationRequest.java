package com.climateguard.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ObservationRequest {
    @NotNull(message = "Region ID is required")
    private Long regionId;
    @NotNull(message = "Observation time is required")
    private LocalDateTime observedAt;
    private BigDecimal temperatureC;
    @PositiveOrZero private BigDecimal rainfallMm;
    @DecimalMin("0.0") @DecimalMax("100.0") private BigDecimal humidityPct;
    @PositiveOrZero private BigDecimal windSpeedMs;
    @Size(max = 50) private String source;
}
