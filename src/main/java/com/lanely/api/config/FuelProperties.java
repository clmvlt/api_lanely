package com.lanely.api.config;

import com.lanely.api.entity.enums.FuelType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuration for the French government open-data fuel price feed (e.g. data.economie.gouv.fr).
 * The source is fully pluggable: the dataset endpoint, the JSON record/field layout and the field
 * names holding the price and the reference date are all driven by properties, so swapping the
 * government dataset is configuration only.
 *
 * <p>The feed exposes several fuels per record (diesel, petrol, LPG, ...). {@code columns} maps each
 * source price/date field pair to a {@link FuelType}, so a single fetch ingests every fuel at once.
 * When {@code columns} is empty the legacy single {@code priceField}/{@code dateField}/{@code fuelType}
 * triple is used as a one-column fallback (keeps dev seeding and tests simple).
 */
@ConfigurationProperties(prefix = "app.fuel")
public record FuelProperties(

        String baseUrl,
        String datasetPath,
        Duration connectTimeout,
        Duration readTimeout,
        String defaultSource,
        FuelType fuelType,
        String recordsField,
        String fieldsField,
        String priceField,
        String dateField,
        List<FuelColumn> columns
) {

    /**
     * Maps one source price/date field pair to the {@link FuelType} it represents. Each fuel must
     * map to a distinct {@link FuelType}: the index is keyed by {@code (fuelType, referenceDate,
     * source)}, so two columns sharing a type would collide on upsert.
     */
    public record FuelColumn(FuelType fuelType, String priceField, String dateField) {
    }

    public Duration connectTimeoutOrDefault() {
        return connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
    }

    public Duration readTimeoutOrDefault() {
        return readTimeout == null ? Duration.ofSeconds(30) : readTimeout;
    }

    public String defaultSourceOrDefault() {
        return defaultSource == null || defaultSource.isBlank() ? "data.economie.gouv.fr" : defaultSource;
    }

    public FuelType fuelTypeOrDefault() {
        return fuelType == null ? FuelType.DIESEL : fuelType;
    }

    public String recordsFieldOrDefault() {
        return recordsField == null || recordsField.isBlank() ? "results" : recordsField;
    }

    public String priceFieldOrDefault() {
        return priceField == null || priceField.isBlank() ? "prix" : priceField;
    }

    public String dateFieldOrDefault() {
        return dateField == null || dateField.isBlank() ? "date" : dateField;
    }

    /**
     * The fuel columns to ingest. Falls back to a single column built from the legacy
     * {@code fuelType}/{@code priceField}/{@code dateField} when none are configured.
     */
    public List<FuelColumn> columnsOrDefault() {
        if (columns == null || columns.isEmpty()) {
            return List.of(new FuelColumn(fuelTypeOrDefault(), priceFieldOrDefault(), dateFieldOrDefault()));
        }
        return columns;
    }
}
