package com.example.incident.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SyntheticIncidentRepositoryTest {

    @Inject
    SyntheticIncidentRepository repository;

    @Test
    void loadReturnsSampleIncidentDataset() {
        var dataset = repository.load("inc-2026-0042");
        assertNotNull(dataset);
        assertEquals("inc-2026-0042", dataset.incidentId());
    }

    @Test
    void loadReturnsNullForUnknownIncidentId() {
        var dataset = repository.load("inc-does-not-exist");
        assertNull(dataset);
    }

    @Test
    void loadedIncidentEventsHaveRequiredFields() {
        var dataset = repository.load("inc-2026-0042");
        assertNotNull(dataset);
        assertFalse(dataset.events().isEmpty());
        for (var event : dataset.events()) {
            assertNotNull(event.id(), "event.id must not be null");
            assertNotNull(event.timestamp(), "event.timestamp must not be null");
            assertNotNull(event.service(), "event.service must not be null");
            assertNotNull(event.type(), "event.type must not be null");
            assertNotNull(event.message(), "event.message must not be null");
        }
    }

    @Test
    void loadReturnsSecondIncidentDataset() {
        var dataset = repository.load("inc-2026-0099");
        assertNotNull(dataset);
        assertEquals("inc-2026-0099", dataset.incidentId());
    }

    @Test
    void listSummariesReturnsBothIncidents() {
        var summaries = repository.listSummaries();
        assertEquals(2, summaries.size());
    }

    @Test
    void listSummariesContainsBothIncidentIds() {
        var ids = repository.listSummaries().stream()
            .map(s -> s.id())
            .collect(Collectors.toSet());
        assertTrue(ids.contains("inc-2026-0042"));
        assertTrue(ids.contains("inc-2026-0099"));
    }
}
