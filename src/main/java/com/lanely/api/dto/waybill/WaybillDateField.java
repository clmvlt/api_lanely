package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "WaybillDateField",
        description = "Which date a waybill list date range filters on. "
                + "PICKUP and DELIVERY filter on the planned pickup/delivery instants; "
                + "DOCK filters on the dock-entry instant (date de passage à quai).")
public enum WaybillDateField {
    PICKUP,
    DELIVERY,
    DOCK
}
