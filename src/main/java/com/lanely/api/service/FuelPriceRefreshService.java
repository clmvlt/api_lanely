package com.lanely.api.service;

import com.lanely.api.entity.FuelPriceIndex;
import com.lanely.api.integration.fuel.FuelClient;
import com.lanely.api.integration.fuel.FuelPriceSample;
import com.lanely.api.repository.FuelPriceIndexRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class FuelPriceRefreshService {

    private final FuelClient fuelClient;
    private final FuelPriceIndexRepository repository;

    public FuelPriceRefreshService(FuelClient fuelClient, FuelPriceIndexRepository repository) {
        this.fuelClient = fuelClient;
        this.repository = repository;
    }

    /**
     * Fetches the government feed and upserts each observation by (fuelType, referenceDate, source),
     * so repeated runs are idempotent. Returns the number of rows created or updated.
     */
    @Transactional
    public int refresh() {
        List<FuelPriceSample> samples = fuelClient.fetchLatest();
        int count = 0;
        for (FuelPriceSample sample : samples) {
            FuelPriceIndex index = repository
                    .findByFuelTypeAndReferenceDateAndSource(sample.fuelType(), sample.referenceDate(), sample.source())
                    .orElseGet(FuelPriceIndex::new);
            index.setFuelType(sample.fuelType());
            index.setPrice(sample.price());
            index.setCurrency("EUR");
            index.setReferenceDate(sample.referenceDate());
            index.setSource(sample.source());
            index.setFetchedAt(Instant.now());
            repository.save(index);
            count++;
        }
        return count;
    }
}
