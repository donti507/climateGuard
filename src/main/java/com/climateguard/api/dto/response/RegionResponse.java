package com.climateguard.api.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RegionResponse {
    private Long id;
    private String name;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal areaKm2;
    private LocalDateTime createdAt;
}
