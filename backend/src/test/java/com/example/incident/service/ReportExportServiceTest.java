package com.example.incident.service;

import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import com.example.incident.domain.TimelineEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportExportServiceTest {

    private final ReportExportService service = new ReportExportService();

    private IncidentReport sampleReport() {
        var timeline = List.of(new TimelineEntry(
            Instant.parse("2026-04-17T09:00:12Z"),
            "db_error observed",
            "First db_error signal",
            List.of("E-evt-001")
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

    @Test
    void toMarkdownContainsIncidentIdInHeading() {
        String markdown = service.toMarkdown(sampleReport());
        assertTrue(markdown.contains("# Incident Report inc-2026-0042"));
    }

    @Test
    void toMarkdownContainsTimelineAndFindingsSections() {
        String markdown = service.toMarkdown(sampleReport());
        assertTrue(markdown.contains("## Timeline"));
        assertTrue(markdown.contains("## Top RCA Findings"));
    }

    @Test
    void toMarkdownIncludesEachFindingHypothesisAndConfidence() {
        String markdown = service.toMarkdown(sampleReport());
        assertTrue(markdown.contains("RCA-1"));
        assertTrue(markdown.contains("Primary key hot partition caused payment timeout."));
        assertTrue(markdown.contains("0.78"));
    }
}
