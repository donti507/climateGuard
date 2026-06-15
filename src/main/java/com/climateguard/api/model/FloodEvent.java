package com.climateguard.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flood_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FloodEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Column(name = "event_type", nullable = false, length = 20) private String eventType;
    @Column(nullable = false, length = 20) private String severity;
    @Column(name = "risk_score", precision = 4, scale = 2) private BigDecimal riskScore;
    @Column(name = "started_at") private LocalDateTime startedAt;
    @Column(name = "ended_at") private LocalDateTime endedAt;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
}
