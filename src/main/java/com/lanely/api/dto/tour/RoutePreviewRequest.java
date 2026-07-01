package com.lanely.api.dto.tour;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(name = "RoutePreviewRequest", description = "Preview the route for a custom stop order WITHOUT persisting it. "
        + "Used for real-time display while the user reorders the tour on the frontend.")
public record RoutePreviewRequest(

        @Schema(description = "Ordered waybill identifiers to preview the route for", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        List<UUID> waybillIds
) {
}
