package com.factory.events.concurrency;

import com.factory.events.dto.EventRequest;
import com.factory.events.repository.MachineEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentIngestionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MachineEventRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void concurrentIngestion_shouldNotCreateDuplicates() throws Exception {

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        EventRequest event = new EventRequest();
        event.setEventId("CONCURRENT-1");
        event.setMachineId("M-001");
        event.setFactoryId("F-01");
        event.setLineId("L-01");
        event.setEventTime(Instant.now());
        event.setDurationMs(1000L);
        event.setDefectCount(0);

        String payload = objectMapper.writeValueAsString(List.of(event));

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(
                            post("/events/batch")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload)
                    ).andExpect(status().isOk());
                } catch (Exception ignored) {
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Only ONE record should exist
        assertThat(repository.count()).isEqualTo(1);
    }
}
