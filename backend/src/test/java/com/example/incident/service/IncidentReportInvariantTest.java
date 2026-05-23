package com.example.incident.service;

import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentReport;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class IncidentReportInvariantTest {

    @Inject
    IncidentReportService reportService;

    private IncidentReport report;
    private IncidentReport report2;

    @BeforeEach
    void buildReports() {
        report = reportService.buildReport("inc-2026-0042");
        report2 = reportService.buildReport("inc-2026-0099");
    }

    @Test
    void allFindingEvidenceIdsExistInIncidentEvidence() {
        Set<String> evidenceIds = report.evidence().stream()
            .map(Evidence::id)
            .collect(Collectors.toSet());

        for (var finding : report.topFindings()) {
            for (var evidenceId : finding.supportingEvidenceIds()) {
                assertTrue(evidenceIds.contains(evidenceId),
                    "Finding " + finding.id() + " references evidence ID " + evidenceId
                        + " which is not present in the evidence list");
            }
        }
    }

    @Test
    void timelineEntriesAreInChronologicalOrder() {
        var timeline = report.timeline();
        for (int i = 1; i < timeline.size(); i++) {
            Instant prev = timeline.get(i - 1).timestamp();
            Instant curr = timeline.get(i).timestamp();
            assertFalse(curr.isBefore(prev),
                "Timeline entry at index " + i + " (" + curr + ") is before entry at index "
                    + (i - 1) + " (" + prev + ")");
        }
    }

    @Test
    void reviewRequirementIsNonNullAndNonEmpty() {
        assertNotNull(report.reviewRequirement());
        assertFalse(report.reviewRequirement().isBlank());
    }

    @Test
    void noFindingHasEmptyEvidenceIds() {
        for (var finding : report.topFindings()) {
            assertFalse(finding.supportingEvidenceIds().isEmpty(),
                "Finding " + finding.id() + " has empty supportingEvidenceIds");
        }
    }

    @Test
    void secondIncidentAllFindingEvidenceIdsExistInEvidence() {
        Set<String> evidenceIds = report2.evidence().stream()
            .map(Evidence::id)
            .collect(Collectors.toSet());

        for (var finding : report2.topFindings()) {
            for (var evidenceId : finding.supportingEvidenceIds()) {
                assertTrue(evidenceIds.contains(evidenceId),
                    "Finding " + finding.id() + " references evidence ID " + evidenceId
                        + " which is not present in the evidence list");
            }
        }
    }

    @Test
    void secondIncidentReviewRequirementIsNonNullAndNonEmpty() {
        assertNotNull(report2.reviewRequirement());
        assertFalse(report2.reviewRequirement().isBlank());
    }

    @Test
    void secondIncidentReturnsFewerThanThreeFindings() {
        assertTrue(report2.topFindings().size() < 3,
            "Expected fewer than 3 findings for inc-2026-0099 (no db_error events, RCA-1 should be filtered), "
                + "but got " + report2.topFindings().size());
    }
}
