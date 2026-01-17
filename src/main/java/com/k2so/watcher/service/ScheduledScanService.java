package com.k2so.watcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledScanService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledScanService.class);

    private final NetworkScannerService networkScannerService;

    @Value("${k2so.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public ScheduledScanService(NetworkScannerService networkScannerService) {
        this.networkScannerService = networkScannerService;
    }

    @Scheduled(cron = "${k2so.scheduler.cron:0 0 2 * * *}")
    public void performScheduledScan() {
        if (!schedulerEnabled) {
            logger.debug("Scheduled scanning is disabled");
            return;
        }

        if (networkScannerService.isScanInProgress()) {
            logger.warn("Skipping scheduled scan - another scan is already in progress");
            return;
        }

        logger.info("Starting scheduled network scan");
        try {
            networkScannerService.startScan("SCHEDULED");
        } catch (Exception e) {
            logger.error("Error during scheduled scan", e);
        }
    }
}
