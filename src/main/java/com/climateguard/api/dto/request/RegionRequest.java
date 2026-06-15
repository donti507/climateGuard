package com.climateguard.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RegionRequest {
    @NotBlank(message = "Region name is required") @Size(max = 100)
    private String name;
    @NotBlank(message = "Country is required") @Size(max = 100)
    private String country;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private BigDecimal latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal longitude;
    @Positive private BigDecimal areaKm2;
}
