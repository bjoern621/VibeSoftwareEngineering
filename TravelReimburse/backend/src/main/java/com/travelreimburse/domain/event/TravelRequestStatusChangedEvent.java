package com.travelreimburse.domain.event;

import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.model.TravelRequestStatus;

/**
 * Domain Event für Statusänderungen von Reiseanträgen
 * Wird publiziert, wenn sich der Status eines Reiseantrags ändert
 */
public class TravelRequestStatusChangedEvent {
    private final TravelRequest travelRequest;
    private final TravelRequestStatus oldStatus;
    private final TravelRequestStatus newStatus;

    public TravelRequestStatusChangedEvent(TravelRequest travelRequest,
                                          TravelRequestStatus oldStatus,
                                          TravelRequestStatus newStatus) {
        this.travelRequest = travelRequest;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public TravelRequest getTravelRequest() {
        return travelRequest;
    }

    public TravelRequestStatus getOldStatus() {
        return oldStatus;
    }

    public TravelRequestStatus getNewStatus() {
        return newStatus;
    }
}

