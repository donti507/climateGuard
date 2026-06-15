package com.climateguard.api.mapper;

import com.climateguard.api.dto.request.RegionRequest;
import com.climateguard.api.dto.response.RegionResponse;
import com.climateguard.api.model.Region;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RegionMapper {
    Region toEntity(RegionRequest request);
    RegionResponse toResponse(Region region);
    void updateEntity(RegionRequest request, @MappingTarget Region region);
}
