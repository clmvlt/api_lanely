package com.lanely.api.integration.fuel;

import com.fasterxml.jackson.databind.JsonNode;
import com.lanely.api.config.FuelProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes a government dataset JSON payload into normalized {@link FuelPriceSample}s. The shape is
 * driven by {@link FuelProperties}: {@code recordsField} locates the array of records, the optional
 * {@code fieldsField} locates the object holding the values inside each record (Opendatasoft v1 uses
 * {@code fields}; v2 exposes them at the record root), and each configured {@code columns} entry
 * names the price (EUR/L) and reference date field for one {@link com.lanely.api.entity.enums.FuelType}.
 * Every record yields one sample per column whose price and date are present; unparseable values are
 * skipped, never fatal.
 */
@Component
public class FuelPriceParser {

    private static final Logger log = LoggerFactory.getLogger(FuelPriceParser.class);

    private final FuelProperties properties;

    public FuelPriceParser(FuelProperties properties) {
        this.properties = properties;
    }

    public List<FuelPriceSample> parse(JsonNode body) {
        List<FuelPriceSample> samples = new ArrayList<>();
        if (body == null) {
            return samples;
        }
        JsonNode records = body.path(properties.recordsFieldOrDefault());
        if (!records.isArray()) {
            log.warn("Fuel dataset payload has no '{}' array; nothing to ingest", properties.recordsFieldOrDefault());
            return samples;
        }
        String fieldsField = properties.fieldsField();
        List<FuelProperties.FuelColumn> columns = properties.columnsOrDefault();
        for (JsonNode record : records) {
            JsonNode fields = fieldsField == null || fieldsField.isBlank() ? record : record.path(fieldsField);
            for (FuelProperties.FuelColumn column : columns) {
                FuelPriceSample sample = toSample(fields, column);
                if (sample != null) {
                    samples.add(sample);
                }
            }
        }
        return samples;
    }

    private FuelPriceSample toSample(JsonNode fields, FuelProperties.FuelColumn column) {
        JsonNode priceNode = fields.path(column.priceField());
        JsonNode dateNode = fields.path(column.dateField());
        if (priceNode.isMissingNode() || priceNode.isNull() || dateNode.isMissingNode() || dateNode.isNull()) {
            return null;
        }
        BigDecimal price = parsePrice(priceNode);
        LocalDate date = parseDate(dateNode.asText());
        if (price == null || date == null) {
            return null;
        }
        return new FuelPriceSample(column.fuelType(), price, date, properties.defaultSourceOrDefault());
    }

    private BigDecimal parsePrice(JsonNode priceNode) {
        try {
            if (priceNode.isNumber()) {
                return priceNode.decimalValue();
            }
            return new BigDecimal(priceNode.asText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            log.debug("Skipping fuel record with unparseable price '{}'", priceNode.asText());
            return null;
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String trimmed = raw.trim();
            return LocalDate.parse(trimmed.length() >= 10 ? trimmed.substring(0, 10) : trimmed);
        } catch (RuntimeException ex) {
            log.debug("Skipping fuel record with unparseable date '{}'", raw);
            return null;
        }
    }
}
