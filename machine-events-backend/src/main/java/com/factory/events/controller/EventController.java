package com.factory.events.controller;

import com.factory.events.dto.EventRequest;
import com.factory.events.dto.IngestResult;
import com.factory.events.dto.StatsResponse;
import com.factory.events.dto.TopDefectLineResponse;
import com.factory.events.service.EventIngestionService;
import com.factory.events.service.StatsService;
import com.factory.events.service.TopDefectLinesService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventIngestionService ingestionService;
    private final StatsService statsService;
    private final TopDefectLinesService topDefectLinesService;

    public EventController(EventIngestionService ingestionService,
                           StatsService statsService,
                           TopDefectLinesService topDefectLinesService) {
        this.ingestionService = ingestionService;
        this.statsService = statsService;
        this.topDefectLinesService = topDefectLinesService;
    }

    @PostMapping("/batch")
    public IngestResult ingestBatch(@RequestBody List<EventRequest> events) {
        return ingestionService.ingest(events);
    }

    @GetMapping("/stats")
    public StatsResponse getStats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return statsService.getStats(machineId, start, end);
    }

    @GetMapping("/stats/top-defect-lines")
    public List<TopDefectLineResponse> topDefectLines(
            @RequestParam String factoryId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return topDefectLinesService.getTopDefectLines(factoryId, from, to, limit);
    }
}
