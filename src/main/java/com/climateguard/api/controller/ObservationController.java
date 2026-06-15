package com.climateguard.api.controller;

import com.climateguard.api.dto.request.ObservationRequest;
import com.climateguard.api.dto.response.ObservationResponse;
import com.climateguard.api.dto.response.ObservationStatsResponse;
import com.climateguard.api.service.ObservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/observations")
@RequiredArgsConstructor
@Tag(name = "Climate Observations", description = "Temperature, rainfall, and weather data")
@SecurityRequirement(name = "Bearer Authentication")
public class ObservationController {

    private final ObservationService observationService;

    @GetMapping
    @Operation(summary = "List all observations (paginated)")
    public ResponseEntity<Page<ObservationResponse>> getAll(
            @PageableDefault(size = 20, sort = "observedAt") Pageable pageable) {
        return ResponseEntity.ok(observationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an observation by ID")
    public ResponseEntity<ObservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(observationService.getById(id));
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get observations for a region with optional filters")
    public ResponseEntity<Page<ObservationResponse>> getByRegion(
            @PathVariable Long regionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String source,
            @PageableDefault(size = 20, sort = "observedAt") Pageable pageable) {
        return ResponseEntity.ok(observationService.getByRegion(regionId, from, to, source, pageable));
    }

    @GetMapping("/region/{regionId}/stats")
    @Operation(summary = "Get climate statistics for a region (avg temp, total rainfall, etc.)")
    public ResponseEntity<ObservationStatsResponse> getStats(
            @PathVariable Long regionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(observationService.getStatsForRegion(regionId, from, to));
    }

    @PostMapping
    @Operation(summary = "Add a new observation [ADMIN]")
    public ResponseEntity<ObservationResponse> create(@Valid @RequestBody ObservationRequest request) {
        return new ResponseEntity<>(observationService.create(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an observation [ADMIN]")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        observationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
