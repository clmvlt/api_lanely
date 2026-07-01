package com.lanely.api.mapper;

import com.lanely.api.dto.geo.CoordinateDto;
import com.lanely.api.dto.geo.RouteInfoDto;
import com.lanely.api.dto.geo.SkippedVisitDto;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.integration.ors.dto.OrsCoordinate;
import com.lanely.api.integration.ors.dto.OrsOptimizeResponse;

import java.util.List;

public final class GeoMapper {

    private GeoMapper() {
    }

    public static OrsCoordinate toOrsCoordinate(CoordinateDto dto) {
        if (dto == null || !dto.isComplete()) {
            return null;
        }
        return new OrsCoordinate(dto.latitude(), dto.longitude());
    }

    public static OrsCoordinate toOrsCoordinate(GeoPoint point) {
        if (point == null || !point.isComplete()) {
            return null;
        }
        return new OrsCoordinate(point.getLatitude(), point.getLongitude());
    }

    public static CoordinateDto toCoordinateDto(GeoPoint point) {
        if (point == null || !point.isComplete()) {
            return null;
        }
        return new CoordinateDto(point.getLatitude(), point.getLongitude());
    }

    public static GeoPoint toGeoPoint(CoordinateDto dto) {
        if (dto == null) {
            return new GeoPoint();
        }
        return new GeoPoint(dto.latitude(), dto.longitude());
    }

    public static RouteInfoDto toRouteInfoDto(RouteInfo route) {
        if (route == null || !route.isComputed()) {
            return null;
        }
        return new RouteInfoDto(route.getDistanceMeters(), route.getDurationSeconds(),
                route.getGeometryPolyline(), route.getComputedAt());
    }

    public static List<SkippedVisitDto> toSkipped(OrsOptimizeResponse response) {
        if (response.skippedVisits() == null) {
            return List.of();
        }
        return response.skippedVisits().stream()
                .map(v -> new SkippedVisitDto(v.visitId(), v.name(), v.reason(), v.snapDistanceMeters()))
                .toList();
    }
}
