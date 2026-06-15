package com.climateguard.api.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FloodEventResponse {
    private Long id;
    private Long regionId;
    private String regionName;
    private String eventType;
    private String severity;
    private BigDecimal riskScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String description;
    private LocalDateTime createdAt;
}
