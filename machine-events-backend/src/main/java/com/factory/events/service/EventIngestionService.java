package com.factory.events.service;

import com.factory.events.dto.EventRequest;
import com.factory.events.dto.IngestResult;
import com.factory.events.entity.MachineEvent;
import com.factory.events.repository.MachineEventRepository;
import com.factory.events.util.HashUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventIngestionService {

    private final MachineEventRepository repository;

    public EventIngestionService(MachineEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IngestResult ingest(List<EventRequest> events) {

        IngestResult result = new IngestResult();

        for (EventRequest request : events) {
            try {
                EventValidator.validate(request);

                String hash = HashUtil.payloadHash(request);
                Instant receivedTime = Instant.now();

                Optional<MachineEvent> existingOpt =
                        repository.findById(request.getEventId());

                if (existingOpt.isEmpty()) {
                    // INSERT
                    MachineEvent entity = toEntity(request, receivedTime, hash);
                    repository.save(entity);
                    result.accepted++;
                } else {
                    MachineEvent existing = existingOpt.get();

                    if (existing.getPayloadHash().equals(hash)) {
                        // DEDUPE
                        result.deduped++;
                    } else if (receivedTime.isAfter(existing.getReceivedTime())) {
                        // UPDATE
                        updateEntity(existing, request, receivedTime, hash);
                        repository.save(existing);
                        result.updated++;
                    } else {
                        // IGNORE older update
                        result.deduped++;
                    }
                }

            } catch (IllegalArgumentException ex) {
                result.rejected++;
                result.rejections.add(
                        new IngestResult.Rejection(
                                request.getEventId(),
                                ex.getMessage()
                        )
                );
            }
        }

        return result;
    }

    private MachineEvent toEntity(EventRequest r, Instant receivedTime, String hash) {
        MachineEvent e = new MachineEvent();
        e.setEventId(r.getEventId());
        e.setMachineId(r.getMachineId());
        e.setFactoryId(r.getFactoryId());
        e.setLineId(r.getLineId());
        e.setEventTime(r.getEventTime());
        e.setReceivedTime(receivedTime);
        e.setDurationMs(r.getDurationMs());
        e.setDefectCount(r.getDefectCount());
        e.setPayloadHash(hash);
        return e;
    }

    private void updateEntity(MachineEvent e, EventRequest r,
                              Instant receivedTime, String hash) {
        e.setMachineId(r.getMachineId());
        e.setFactoryId(r.getFactoryId());
        e.setLineId(r.getLineId());
        e.setEventTime(r.getEventTime());
        e.setDurationMs(r.getDurationMs());
        e.setDefectCount(r.getDefectCount());
        e.setReceivedTime(receivedTime);
        e.setPayloadHash(hash);
    }
}
