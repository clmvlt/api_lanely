package com.lanely.api.integration.fuel;

import com.lanely.api.service.FuelPriceRefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.fuel", name = "scheduler-enabled", havingValue = "true")
public class FuelPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(FuelPriceScheduler.class);

    private final FuelPriceRefreshService refreshService;

    public FuelPriceScheduler(FuelPriceRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @Scheduled(cron = "${app.fuel.refresh-cron}")
    public void refresh() {
        try {
            int ingested = refreshService.refresh();
            log.info("Scheduled fuel price refresh ingested {} observation(s)", ingested);
        } catch (RuntimeException ex) {
            log.warn("Scheduled fuel price refresh failed; keeping previously stored prices", ex);
        }
    }
}
