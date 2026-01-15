package com.concertcomparison.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler für automatisches Cleanup abgelaufener Holds.
 * Läuft alle 2 Minuten (konfigurierbar via Cron).
 */
@Component
public class HoldCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HoldCleanupScheduler.class);

    private final HoldCleanupService holdCleanupService;

    public HoldCleanupScheduler(HoldCleanupService holdCleanupService) {
        this.holdCleanupService = holdCleanupService;
    }

    /**
     * Scheduled Task: Cleanup expired holds.
     * Runs every 2 minutes.
     * 
     * Cron Expression: 0 slash-two asterisk asterisk asterisk asterisk = Second 0, every 2 minutes
     * 
     * Alternative Frequenzen:
     * - Every 30 seconds: slash-30 asterisk asterisk asterisk asterisk asterisk
     * - Every 1 minute: 0 asterisk asterisk asterisk asterisk asterisk
     * - Every 5 minutes: 0 slash-5 asterisk asterisk asterisk asterisk
     */
    @Scheduled(cron = "0 */2 * * * *")
    public void scheduleHoldCleanup() {
        logger.debug("Hold cleanup scheduler triggered");
        
        try {
            int cleaned = holdCleanupService.cleanupExpiredHolds();
            
            if (cleaned > 0) {
                logger.info("Hold cleanup scheduler: {} holds cleaned", cleaned);
            }
            
        } catch (Exception e) {
            logger.error("Error in hold cleanup scheduler: {}", e.getMessage(), e);
        }
    }
}
