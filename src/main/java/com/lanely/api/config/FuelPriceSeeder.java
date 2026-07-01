package com.lanely.api.config;

import com.lanely.api.entity.FuelPriceIndex;
import com.lanely.api.entity.enums.FuelType;
import com.lanely.api.repository.FuelPriceIndexRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Seeds a few recent fuel price observations (one series per supported {@link FuelType}) so pricing
 * with fuel surcharge works offline in dev, without calling the government API. Idempotent: upserts
 * by (fuelType, referenceDate, source). Gated by {@code app.fuel.seed-enabled} (enabled in dev/local
 * only, never in prod).
 */
@Component
@ConditionalOnProperty(prefix = "app.fuel", name = "seed-enabled", havingValue = "true")
public class FuelPriceSeeder implements ApplicationRunner {

    private static final String SOURCE = "SEED";

    private record PriceSeed(int weeksAgo, BigDecimal price) {
    }

    private record FuelSeed(FuelType fuelType, List<PriceSeed> series) {
    }

    private static final List<FuelSeed> SEEDS = List.of(
            new FuelSeed(FuelType.DIESEL, List.of(
                    new PriceSeed(2, new BigDecimal("1.7450")),
                    new PriceSeed(1, new BigDecimal("1.7680")),
                    new PriceSeed(0, new BigDecimal("1.7820")))),
            new FuelSeed(FuelType.PETROL, List.of(
                    new PriceSeed(2, new BigDecimal("1.8550")),
                    new PriceSeed(1, new BigDecimal("1.8720")),
                    new PriceSeed(0, new BigDecimal("1.8910")))),
            new FuelSeed(FuelType.LPG, List.of(
                    new PriceSeed(2, new BigDecimal("0.9180")),
                    new PriceSeed(1, new BigDecimal("0.9240")),
                    new PriceSeed(0, new BigDecimal("0.9310")))),
            new FuelSeed(FuelType.OTHER, List.of(
                    new PriceSeed(2, new BigDecimal("0.8350")),
                    new PriceSeed(1, new BigDecimal("0.8420")),
                    new PriceSeed(0, new BigDecimal("0.8490"))))
    );

    private final FuelPriceIndexRepository repository;

    public FuelPriceSeeder(FuelPriceIndexRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LocalDate monday = LocalDate.ofInstant(Instant.now(), ZoneOffset.UTC).with(java.time.DayOfWeek.MONDAY);
        for (FuelSeed fuel : SEEDS) {
            for (PriceSeed seed : fuel.series()) {
                LocalDate referenceDate = monday.minusWeeks(seed.weeksAgo());
                FuelPriceIndex index = repository
                        .findByFuelTypeAndReferenceDateAndSource(fuel.fuelType(), referenceDate, SOURCE)
                        .orElseGet(FuelPriceIndex::new);
                index.setFuelType(fuel.fuelType());
                index.setPrice(seed.price());
                index.setCurrency("EUR");
                index.setReferenceDate(referenceDate);
                index.setSource(SOURCE);
                index.setFetchedAt(Instant.now());
                repository.save(index);
            }
        }
    }
}
