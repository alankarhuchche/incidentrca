package com.example.incident.service;

import com.example.incident.domain.Evidence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RcaRankingServiceTest {

    private final RcaRankingService service = new RcaRankingService();

    @Test
    void ranksTopThreeByConfidenceAndPreservesEvidenceTraceability() {
        List<Evidence> evidence = List.of(
            new Evidence("E-db-1", "db_error", "db", "evt1", Instant.parse("2026-04-17T09:01:00Z"), "d"),
            new Evidence("E-db-2", "db_error", "db", "evt2", Instant.parse("2026-04-17T09:01:30Z"), "d"),
            new Evidence("E-pg-1", "payment_gateway", "pg", "evt3", Instant.parse("2026-04-17T09:02:00Z"), "d"),
            new Evidence("E-app-1", "application_log", "app", "evt4", Instant.parse("2026-04-17T09:03:00Z"), "d")
        );

        var findings = service.rankTopFindings(evidence);

        assertEquals(3, findings.size());
        assertTrue(findings.get(0).confidence() >= findings.get(1).confidence());
        assertTrue(findings.get(1).confidence() >= findings.get(2).confidence());
        assertEquals("RCA-1", findings.get(0).id());
        assertTrue(findings.stream().allMatch(f -> !f.supportingEvidenceIds().isEmpty()));
    }
}
