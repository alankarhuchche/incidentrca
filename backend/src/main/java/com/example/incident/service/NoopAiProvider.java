package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NoopAiProvider implements AiProvider {

    @Override
    public AiExplanation explain(IncidentReport report) {
        RcaFinding topFinding = report.topFindings().isEmpty() ? null : report.topFindings().get(0);

        String summary;
        String rootCauseSummary;
        if (topFinding != null) {
            summary = String.format(
                "Incident %s reached status '%s'. Deterministic analysis identified %d finding(s). " +
                "Highest-confidence finding (%.0f%%): %s",
                report.incidentId(), report.status(), report.topFindings().size(),
                topFinding.confidence() * 100, topFinding.hypothesis()
            );
            rootCauseSummary = topFinding.hypothesis();
        } else {
            summary = String.format(
                "Incident %s reached status '%s'. No findings with supporting evidence were identified.",
                report.incidentId(), report.status()
            );
            rootCauseSummary = "No root cause identified.";
        }

        return new AiExplanation(
            report.incidentId(),
            summary,
            rootCauseSummary,
            report.reviewRequirement(),
            "noop",
            false
        );
    }
}
