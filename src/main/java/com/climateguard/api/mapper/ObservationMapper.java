package com.climateguard.api.mapper;

import com.climateguard.api.dto.response.ObservationResponse;
import com.climateguard.api.model.ClimateObservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ObservationMapper {
    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "region.name", target = "regionName")
    ObservationResponse toResponse(ClimateObservation observation);
}
