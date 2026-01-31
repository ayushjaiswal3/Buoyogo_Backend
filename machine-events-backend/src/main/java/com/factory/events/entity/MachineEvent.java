package com.factory.events.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "machine_events")
public class MachineEvent {

    @Id
    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Column(name = "factory_id", nullable = false)
    private String factoryId;

    @Column(name = "line_id", nullable = false)
    private String lineId;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "received_time", nullable = false)
    private Instant receivedTime;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "defect_count", nullable = false)
    private Integer defectCount;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;
}
