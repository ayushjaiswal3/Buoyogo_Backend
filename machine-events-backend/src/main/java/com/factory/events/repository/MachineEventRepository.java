package com.factory.events.repository;

import com.factory.events.entity.MachineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MachineEventRepository extends JpaRepository<MachineEvent, String> {

    List<MachineEvent> findByMachineIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            String machineId,
            Instant start,
            Instant end
    );

    List<MachineEvent> findByFactoryIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            String factoryId,
            Instant from,
            Instant to
    );
}
