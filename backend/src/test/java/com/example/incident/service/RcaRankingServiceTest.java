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
    void returnsNoFindingsWhenAllEvidenceIsAbsent() {
        var findings = service.rankTopFindings(List.of());
        assertTrue(findings.isEmpty());
    }

    @Test
    void confidenceScalesWithEvidenceCountUpToCap() {
        List<Evidence> oneDbError = List.of(
            new Evidence("E-db-1", "db_error", "db", "evt1", Instant.parse("2026-04-17T09:01:00Z"), "d")
        );
        var findings = service.rankTopFindings(oneDbError);
        var dbFinding = findings.stream().filter(f -> f.id().equals("RCA-1")).findFirst().orElseThrow();
        assertEquals(0.70, dbFinding.confidence(), 0.001);

        List<Evidence> fiveDbErrors = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            fiveDbErrors.add(new Evidence("E-db-" + i, "db_error", "db", "evt" + i,
                Instant.parse("2026-04-17T09:01:00Z"), "d"));
        }
        var cappedFindings = service.rankTopFindings(fiveDbErrors);
        var cappedFinding = cappedFindings.stream().filter(f -> f.id().equals("RCA-1")).findFirst().orElseThrow();
        assertEquals(0.95, cappedFinding.confidence(), 0.001);
    }

    @Test
    void evidenceIdsAreCappedAtFourPerFinding() {
        List<Evidence> sixDbErrors = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            sixDbErrors.add(new Evidence("E-db-" + i, "db_error", "db", "evt" + i,
                Instant.parse("2026-04-17T09:01:00Z"), "d"));
        }
        var findings = service.rankTopFindings(sixDbErrors);
        var dbFinding = findings.stream().filter(f -> f.id().equals("RCA-1")).findFirst().orElseThrow();
        assertEquals(4, dbFinding.supportingEvidenceIds().size());
    }

    @Test
    void onlyRelevantCategoryContributesToEachHypothesis() {
        List<Evidence> gatewayOnly = List.of(
            new Evidence("E-pg-1", "payment_gateway", "pg", "evt1", Instant.parse("2026-04-17T09:01:00Z"), "d")
        );
        var findings = service.rankTopFindings(gatewayOnly);
        assertEquals(1, findings.size());
        assertEquals("RCA-2", findings.get(0).id());
    }

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
