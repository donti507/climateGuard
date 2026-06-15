package com.climateguard.api.controller;

import com.climateguard.api.dto.request.RegionRequest;
import com.climateguard.api.dto.response.RegionResponse;
import com.climateguard.api.service.RegionService;
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

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
@Tag(name = "Regions", description = "Geographic region management")
@SecurityRequirement(name = "Bearer Authentication")
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    @Operation(summary = "List all regions (paginated, filterable by country)")
    public ResponseEntity<Page<RegionResponse>> getAll(
            @RequestParam(required = false) String country,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(regionService.getAllRegions(country, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a region by ID")
    public ResponseEntity<RegionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.getRegionById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new region [ADMIN]")
    public ResponseEntity<RegionResponse> create(@Valid @RequestBody RegionRequest request) {
        return new ResponseEntity<>(regionService.createRegion(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a region [ADMIN]")
    public ResponseEntity<RegionResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody RegionRequest request) {
        return ResponseEntity.ok(regionService.updateRegion(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a region [ADMIN]")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return ResponseEntity.noContent().build();
    }
}
