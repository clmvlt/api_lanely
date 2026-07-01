package com.lanely.api.mapper;

import com.lanely.api.dto.tour.TourResponse;
import com.lanely.api.dto.tour.TourSummaryResponse;
import com.lanely.api.entity.Tour;
import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.embeddable.RouteInfo;

import java.util.List;

public final class TourMapper {

    private TourMapper() {
    }

    public static TourSummaryResponse toSummary(Tour tour) {
        RouteInfo route = tour.getRoute();
        Long distance = route != null && route.isComputed() ? route.getDistanceMeters() : null;
        Long duration = route != null && route.isComputed() ? route.getDurationSeconds() : null;
        return new TourSummaryResponse(tour.getId(), tour.getReference(), tour.getName(), tour.getStatus(),
                AssigneeMapper.id(tour.getAssignedAccount()), AssigneeMapper.type(tour.getAssignedAccount()),
                AssigneeMapper.name(tour.getAssignedAccount()),
                tour.getVehicle() == null ? null : tour.getVehicle().getId(),
                tour.getPlannedDate(), distance, duration, tour.getCreatedAt());
    }

    public static TourResponse toResponse(Tour tour, List<Waybill> orderedWaybills) {
        return new TourResponse(tour.getId(), tour.getReference(), tour.getName(), tour.getStatus(),
                AssigneeMapper.id(tour.getAssignedAccount()), AssigneeMapper.type(tour.getAssignedAccount()),
                AssigneeMapper.name(tour.getAssignedAccount()),
                tour.getVehicle() == null ? null : tour.getVehicle().getId(),
                CompanyMapper.toAddressDto(tour.getDepot()), GeoMapper.toCoordinateDto(tour.getDepotLocation()),
                tour.getPlannedDate(), tour.getStartedAt(), tour.getCompletedAt(),
                GeoMapper.toRouteInfoDto(tour.getRoute()), tour.getLastOptimizedAt(),
                orderedWaybills.stream().map(WaybillMapper::toSummary).toList(),
                tour.getNotes(), tour.getCreatedAt(), tour.getUpdatedAt());
    }
}
