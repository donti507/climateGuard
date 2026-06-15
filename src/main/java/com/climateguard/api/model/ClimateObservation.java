package com.climateguard.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "climate_observations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClimateObservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Column(name = "observed_at", nullable = false) private LocalDateTime observedAt;
    @Column(name = "temperature_c", precision = 5, scale = 2) private BigDecimal temperatureC;
    @Column(name = "rainfall_mm", precision = 7, scale = 2) private BigDecimal rainfallMm;
    @Column(name = "humidity_pct", precision = 5, scale = 2) private BigDecimal humidityPct;
    @Column(name = "wind_speed_ms", precision = 6, scale = 2) private BigDecimal windSpeedMs;
    @Column(length = 50) private String source;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
}
