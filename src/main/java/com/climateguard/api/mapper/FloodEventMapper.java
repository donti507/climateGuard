package com.climateguard.api.mapper;

import com.climateguard.api.dto.response.FloodEventResponse;
import com.climateguard.api.model.FloodEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FloodEventMapper {
    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "region.name", target = "regionName")
    FloodEventResponse toResponse(FloodEvent event);
}
