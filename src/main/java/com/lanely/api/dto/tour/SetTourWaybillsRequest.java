package com.lanely.api.dto.tour;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(name = "SetTourWaybillsRequest", description = "Set the ordered list of waybills that make up the tour. "
        + "Waybills not in the list are detached from the tour; the list order becomes the stop order.")
public record SetTourWaybillsRequest(

        @Schema(description = "Ordered waybill identifiers (the order is the stop order)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        List<UUID> waybillIds
) {
}
