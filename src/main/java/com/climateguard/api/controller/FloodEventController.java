package com.climateguard.api.controller;

import com.climateguard.api.dto.request.FloodEventRequest;
import com.climateguard.api.dto.response.FloodEventResponse;
import com.climateguard.api.service.FloodEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flood-events")
@RequiredArgsConstructor
@Tag(name = "Flood Events", description = "Historical and predicted flood risk data")
@SecurityRequirement(name = "Bearer Authentication")
public class FloodEventController {

    private final FloodEventService floodEventService;

    @GetMapping
    @Operation(summary = "List all flood events (paginated, filterable)")
    public ResponseEntity<Page<FloodEventResponse>> getAll(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minRisk,
            @PageableDefault(size = 20, sort = "startedAt") Pageable pageable) {
        return ResponseEntity.ok(floodEventService.getAll(severity, type, minRisk, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a flood event by ID")
    public ResponseEntity<FloodEventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(floodEventService.getById(id));
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get all flood events for a region")
    public ResponseEntity<Page<FloodEventResponse>> getByRegion(
            @PathVariable Long regionId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(floodEventService.getByRegion(regionId, pageable));
    }

    @GetMapping("/high-risk")
    @Operation(summary = "Get all events with risk score > 0.7")
    public ResponseEntity<List<FloodEventResponse>> getHighRisk() {
        return ResponseEntity.ok(floodEventService.getHighRiskEvents());
    }

    @PostMapping
    @Operation(summary = "Create a flood event [ADMIN]")
    public ResponseEntity<FloodEventResponse> create(@Valid @RequestBody FloodEventRequest request) {
        return new ResponseEntity<>(floodEventService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a flood event [ADMIN]")
    public ResponseEntity<FloodEventResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody FloodEventRequest request) {
        return ResponseEntity.ok(floodEventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a flood event [ADMIN]")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        floodEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
