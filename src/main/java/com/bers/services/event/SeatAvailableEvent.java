package com.bers.services.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatAvailableEvent {
    private final Long tripId;
    private final String seatNumber;
    private final Long fromStopId;
    private final Long toStopId;
}
