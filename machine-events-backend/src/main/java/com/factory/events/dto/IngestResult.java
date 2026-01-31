package com.factory.events.dto;

import java.util.ArrayList;
import java.util.List;

public class IngestResult {

    public int accepted;
    public int updated;
    public int deduped;
    public int rejected;

    public List<Rejection> rejections = new ArrayList<>();

    public static class Rejection {
        public String eventId;
        public String reason;

        public Rejection(String eventId, String reason) {
            this.eventId = eventId;
            this.reason = reason;
        }
    }
}
