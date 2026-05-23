package com.example.incident.service;

import com.example.incident.domain.Evidence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimelineReconstructionServiceTest {

    private final TimelineReconstructionService service = new TimelineReconstructionService();

    @Test
    void reconstructWithSingleEventProducesSingleEntry() {
        List<Evidence> evidence = List.of(
            new Evidence("E-1", "db_error", "db issue", "evt1", Instant.parse("2026-04-17T09:01:00Z"), "d")
        );

        var timeline = service.reconstruct(evidence);

        assertEquals(1, timeline.size());
        assertEquals("db_error observed", timeline.get(0).title());
        assertEquals(List.of("E-1"), timeline.get(0).evidenceIds());
    }

    @Test
    void reconstructBuildsChronologicalEntriesPerCategory() {
        List<Evidence> evidence = List.of(
            new Evidence("E-2", "db_error", "db issue", "evt2", Instant.parse("2026-04-17T09:01:00Z"), "d"),
            new Evidence("E-1", "application_log", "app warn", "evt1", Instant.parse("2026-04-17T09:00:00Z"), "d"),
            new Evidence("E-3", "db_error", "db issue2", "evt3", Instant.parse("2026-04-17T09:02:00Z"), "d")
        );

        var timeline = service.reconstruct(evidence);

        assertEquals(2, timeline.size());
        assertEquals("application_log observed", timeline.get(0).title());
        assertEquals("db_error observed", timeline.get(1).title());
        assertEquals(List.of("E-2", "E-3"), timeline.get(1).evidenceIds());
    }
}
