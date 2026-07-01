package com.lanely.api.service;

import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.RouteInfo;
import com.lanely.api.integration.ors.RoutingClient;
import com.lanely.api.integration.ors.dto.OrsCoordinate;
import com.lanely.api.integration.ors.dto.OrsRouteRequest;
import com.lanely.api.integration.ors.dto.OrsRouteResponse;
import com.lanely.api.mapper.GeoMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RoutingService {

    private final RoutingClient routingClient;

    public RoutingService(RoutingClient routingClient) {
        this.routingClient = routingClient;
    }

    /**
     * Computes a route through the ordered points and returns a populated {@link RouteInfo}.
     * Returns an empty (not computed) RouteInfo when fewer than two routable points are available.
     */
    public RouteInfo routeOrdered(List<GeoPoint> orderedPoints) {
        List<OrsCoordinate> coordinates = orderedPoints.stream()
                .map(GeoMapper::toOrsCoordinate)
                .filter(c -> c != null)
                .toList();
        if (coordinates.size() < 2) {
            return new RouteInfo();
        }
        OrsRouteResponse response = routingClient.route(OrsRouteRequest.ordered(coordinates));
        return toRouteInfo(response);
    }

    private RouteInfo toRouteInfo(OrsRouteResponse response) {
        RouteInfo info = new RouteInfo();
        info.setDistanceMeters(response.distanceMeters());
        info.setDurationSeconds(response.durationSeconds());
        info.setGeometryPolyline(response.geometryPolyline());
        info.setComputedAt(Instant.now());
        return info;
    }
}
