package com.example.incident.service;

import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import com.example.incident.domain.TimelineEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoopAiProviderTest {

    private final NoopAiProvider provider = new NoopAiProvider();

    private IncidentReport sampleReport() {
        var timeline = List.of(new TimelineEntry(
            Instant.parse("2026-04-17T09:00:12Z"),
            "db_error observed", "First db_error signal", List.of("E-evt-001")
        ));
        var evidence = List.of(new Evidence(
            "E-evt-001", "db_error", "lock wait timeout", "evt-001",
            Instant.parse("2026-04-17T09:00:12Z"), "service=payments-db"
        ));
        var findings = List.of(new RcaFinding(
            "RCA-1",
            "Primary key hot partition caused payment timeout.",
            0.78,
            "Requires human verification.",
            List.of("E-evt-001")
        ));
        return new IncidentReport(
            "inc-2026-0042", "Synthetic payment degradation", "Mitigated",
            "Human review is mandatory before final RCA publication.",
            timeline, evidence, findings
        );
    }

    private IncidentReport reportWithNoFindings() {
        return new IncidentReport(
            "inc-test-empty", "Empty findings incident", "Resolved",
            "Human review is mandatory before final RCA publication.",
            List.of(), List.of(), List.of()
        );
    }

    @Test
    void explainReturnsNonNullExplanation() {
        assertNotNull(provider.explain(sampleReport()));
    }

    @Test
    void explainSetsProviderToNoop() {
        assertEquals("noop", provider.explain(sampleReport()).provider());
    }

    @Test
    void explainSetsAiGeneratedFalse() {
        assertFalse(provider.explain(sampleReport()).aiGenerated());
    }

    @Test
    void explainIncidentIdMatchesReport() {
        assertEquals("inc-2026-0042", provider.explain(sampleReport()).incidentId());
    }

    @Test
    void explainSummaryContainsTopFindingHypothesis() {
        String summary = provider.explain(sampleReport()).summary();
        assertTrue(summary.contains("Primary key hot partition caused payment timeout."),
            "Summary should contain the top finding hypothesis");
    }
}
