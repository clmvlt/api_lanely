package com.lanely.api.integration.fuel;

import com.lanely.api.entity.enums.FuelType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A normalized fuel price observation extracted from the government feed, independent of the source
 * JSON layout. Price is expressed in EUR per liter.
 */
public record FuelPriceSample(FuelType fuelType, BigDecimal price, LocalDate referenceDate, String source) {
}
