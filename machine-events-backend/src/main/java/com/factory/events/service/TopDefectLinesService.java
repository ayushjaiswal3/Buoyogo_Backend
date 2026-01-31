package com.factory.events.service;

import com.factory.events.dto.TopDefectLineResponse;
import com.factory.events.entity.MachineEvent;
import com.factory.events.repository.MachineEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopDefectLinesService {

    private final MachineEventRepository repository;

    public TopDefectLinesService(MachineEventRepository repository) {
        this.repository = repository;
    }

    public List<TopDefectLineResponse> getTopDefectLines(
            String factoryId,
            Instant from,
            Instant to,
            int limit
    ) {

        List<MachineEvent> events =
                repository.findByFactoryIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                        factoryId, from, to
                );

        Map<String, List<MachineEvent>> byLine =
                events.stream()
                        .filter(e -> e.getDefectCount() != -1)
                        .collect(Collectors.groupingBy(MachineEvent::getLineId));

        List<TopDefectLineResponse> result = new ArrayList<>();

        for (Map.Entry<String, List<MachineEvent>> entry : byLine.entrySet()) {
            String lineId = entry.getKey();
            List<MachineEvent> lineEvents = entry.getValue();

            long eventCount = lineEvents.size();
            long totalDefects = lineEvents.stream()
                    .mapToLong(MachineEvent::getDefectCount)
                    .sum();

            double percent =
                    eventCount > 0 ? (totalDefects * 100.0) / eventCount : 0;

            TopDefectLineResponse r = new TopDefectLineResponse();
            r.lineId = lineId;
            r.eventCount = eventCount;
            r.totalDefects = totalDefects;
            r.defectsPercent = Math.round(percent * 100.0) / 100.0;

            result.add(r);
        }

        return result.stream()
                .sorted((a, b) -> Long.compare(b.totalDefects, a.totalDefects))
                .limit(limit)
                .toList();
    }
}
