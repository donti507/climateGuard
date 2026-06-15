package com.climateguard.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "regions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Region {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 100) private String country;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal latitude;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal longitude;
    @Column(name = "area_km2", precision = 10, scale = 2) private BigDecimal areaKm2;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
}
