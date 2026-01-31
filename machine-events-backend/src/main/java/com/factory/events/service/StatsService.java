package com.factory.events.service;

import com.factory.events.dto.StatsResponse;
import com.factory.events.entity.MachineEvent;
import com.factory.events.repository.MachineEventRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class StatsService {

    private final MachineEventRepository repository;

    public StatsService(MachineEventRepository repository) {
        this.repository = repository;
    }

    public StatsResponse getStats(String machineId, Instant start, Instant end) {

        List<MachineEvent> events =
                repository.findByMachineIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                        machineId, start, end
                );

        long eventsCount = events.size();

        long defectsCount = events.stream()
                .filter(e -> e.getDefectCount() != -1)
                .mapToLong(MachineEvent::getDefectCount)
                .sum();

        double windowHours =
                Duration.between(start, end).toSeconds() / 3600.0;

        double avgDefectRate =
                windowHours > 0 ? defectsCount / windowHours : 0;

        StatsResponse response = new StatsResponse();
        response.machineId = machineId;
        response.start = start;
        response.end = end;
        response.eventsCount = eventsCount;
        response.defectsCount = defectsCount;
        response.avgDefectRate = avgDefectRate;
        response.status = avgDefectRate < 2.0 ? "Healthy" : "Warning";

        return response;
    }
}
