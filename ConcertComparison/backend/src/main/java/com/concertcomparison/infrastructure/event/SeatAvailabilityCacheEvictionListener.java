package com.concertcomparison.infrastructure.event;

import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event Listener für automatische Cache-Invalidierung bei Seat-Status-Änderungen.
 * 
 * Dieser Listener horcht auf SeatStatusChangedEvents und invalidiert
 * automatisch den seatAvailability-Cache für das betroffene Concert.
 * 
 * Dadurch erhalten Clients beim nächsten Poll (GET /api/events/{id}/availability)
 * garantiert aktuelle Daten ohne manuelle Cache-Verwaltung.
 * 
 * Architecture:
 * - Infrastructure Layer (darf Spring-Dependencies nutzen)
 * - Asynchrone Verarbeitung (@Async) für Performance
 * - Loose Coupling via Events (Publisher kennt Listener nicht)
 */
@Component
public class SeatAvailabilityCacheEvictionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatAvailabilityCacheEvictionListener.class);
    private static final String CACHE_NAME = "seatAvailability";
    
    private final CacheManager cacheManager;
    
    public SeatAvailabilityCacheEvictionListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    /**
     * Horcht auf SeatStatusChangedEvents und invalidiert den Cache.
     * 
     * Cache-Key ist die concertId (siehe @Cacheable in SeatApplicationService).
     * Nach Eviction liefert der nächste API-Call frische Daten aus der DB.
     * 
     * @Async: Event-Handling blockiert nicht den auslösenden Thread
     * @EventListener: Spring Event Mechanismus (synchron standardmäßig)
     * 
     * @param event SeatStatusChangedEvent mit concertId
     */
    @Async
    @EventListener
    public void handleSeatStatusChanged(SeatStatusChangedEvent event) {
        logger.info("Received SeatStatusChangedEvent: {}", event);
        
        try {
            var cache = cacheManager.getCache(CACHE_NAME);
            
            if (cache != null) {
                // Cache-Key ist die concertId (Long)
                Long concertId = event.getConcertId();
                cache.evict(concertId);
                
                logger.info("Cache evicted: cache={}, concertId={}, reason={}", 
                    CACHE_NAME, concertId, event.getReason());
            } else {
                logger.warn("Cache '{}' not found in CacheManager", CACHE_NAME);
            }
            
        } catch (Exception e) {
            // Fehler loggen, aber nicht werfen (Event-Handling soll nicht Application-Flow blockieren)
            logger.error("Failed to evict cache for SeatStatusChangedEvent: {}", event, e);
        }
    }
}
