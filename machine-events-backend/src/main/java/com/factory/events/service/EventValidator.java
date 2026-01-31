package com.factory.events.service;

import com.factory.events.dto.EventRequest;

import java.time.Duration;
import java.time.Instant;

public class EventValidator {

    private static final long MAX_DURATION_MS = Duration.ofHours(6).toMillis();

    public static void validate(EventRequest event) {

        if (event.getDurationMs() == null || event.getDurationMs() < 0) {
            throw new IllegalArgumentException("INVALID_DURATION");
        }

        if (event.getDurationMs() > MAX_DURATION_MS) {
            throw new IllegalArgumentException("INVALID_DURATION");
        }

        Instant nowPlus15Min = Instant.now().plus(Duration.ofMinutes(15));
        if (event.getEventTime() == null || event.getEventTime().isAfter(nowPlus15Min)) {
            throw new IllegalArgumentException("EVENT_TIME_IN_FUTURE");
        }
    }
}
