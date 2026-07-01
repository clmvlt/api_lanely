package com.lanely.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class RouteInfo {

    @Column(name = "route_distance_meters")
    private Long distanceMeters;

    @Column(name = "route_duration_seconds")
    private Long durationSeconds;

    @Column(name = "route_geometry_polyline", columnDefinition = "text")
    private String geometryPolyline;

    @Column(name = "route_computed_at")
    private Instant computedAt;

    public boolean isComputed() {
        return computedAt != null;
    }

    public Long getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Long distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getGeometryPolyline() {
        return geometryPolyline;
    }

    public void setGeometryPolyline(String geometryPolyline) {
        this.geometryPolyline = geometryPolyline;
    }

    public Instant getComputedAt() {
        return computedAt;
    }

    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}
