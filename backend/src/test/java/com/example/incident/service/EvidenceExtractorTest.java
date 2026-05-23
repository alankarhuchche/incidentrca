package com.example.incident.service;

import com.example.incident.domain.IncidentEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceExtractorTest {

    private final EvidenceExtractor extractor = new EvidenceExtractor();

    private IncidentEvent event(String id, Instant timestamp, String type,
                                String service, String region, String severity, String message) {
        return new IncidentEvent(id, timestamp, "synthetic-source", type, service, region, severity, message, Map.of());
    }

    @Test
    void extractPrefixesEvidenceIdsWithE() {
        var events = List.of(
            event("evt-001", Instant.parse("2026-04-17T09:00:00Z"), "application_log", "svc", "us-central1", "WARN", "msg")
        );
        var evidence = extractor.extract(events);
        assertEquals("E-evt-001", evidence.get(0).id());
    }

    @Test
    void extractSortsEventsByTimestamp() {
        var events = List.of(
            event("evt-003", Instant.parse("2026-04-17T09:02:00Z"), "db_error", "svc", "us-central1", "ERROR", "msg3"),
            event("evt-001", Instant.parse("2026-04-17T09:00:00Z"), "db_error", "svc", "us-central1", "WARN", "msg1"),
            event("evt-002", Instant.parse("2026-04-17T09:01:00Z"), "db_error", "svc", "us-central1", "INFO", "msg2")
        );
        var evidence = extractor.extract(events);
        assertEquals("E-evt-001", evidence.get(0).id());
        assertEquals("E-evt-002", evidence.get(1).id());
        assertEquals("E-evt-003", evidence.get(2).id());
    }

    @Test
    void extractMapsEventTypeToCategory() {
        var events = List.of(
            event("evt-001", Instant.parse("2026-04-17T09:00:00Z"), "db_error", "svc", "us-central1", "ERROR", "msg")
        );
        var evidence = extractor.extract(events);
        assertEquals("db_error", evidence.get(0).category());
    }

    @Test
    void extractFormatsDetailWithServiceRegionSeverity() {
        var events = List.of(
            event("evt-001", Instant.parse("2026-04-17T09:00:00Z"), "db_error", "payments-db", "us-central1", "ERROR", "msg")
        );
        var evidence = extractor.extract(events);
        String detail = evidence.get(0).detail();
        assertTrue(detail.contains("service=payments-db"), "detail missing service");
        assertTrue(detail.contains("region=us-central1"), "detail missing region");
        assertTrue(detail.contains("severity=ERROR"), "detail missing severity");
    }

    @Test
    void extractReturnsEmptyListForNoEvents() {
        var evidence = extractor.extract(List.of());
        assertTrue(evidence.isEmpty());
    }
}
