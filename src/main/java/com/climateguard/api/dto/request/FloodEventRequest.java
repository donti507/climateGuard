package com.climateguard.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FloodEventRequest {
    @NotNull(message = "Region ID is required")
    private Long regionId;
    @NotBlank @Pattern(regexp = "HISTORICAL|PREDICTED", message = "Event type must be HISTORICAL or PREDICTED")
    private String eventType;
    @NotBlank @Pattern(regexp = "LOW|MODERATE|HIGH|CRITICAL", message = "Severity must be LOW, MODERATE, HIGH or CRITICAL")
    private String severity;
    @DecimalMin("0.0") @DecimalMax("1.0") private BigDecimal riskScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String description;
}
