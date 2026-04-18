package com.example.incident.service;

import com.example.incident.domain.IncidentReport;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReportExportService {

    public String toMarkdown(IncidentReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Incident Report ").append(report.incidentId()).append("\n\n")
            .append("**Title:** ").append(report.title()).append("\n\n")
            .append("**Status:** ").append(report.status()).append("\n\n")
            .append("**Review requirement:** ").append(report.reviewRequirement()).append("\n\n")
            .append("## Timeline\n");

        report.timeline().forEach(entry -> builder
            .append("- ").append(entry.timestamp()).append(" — ").append(entry.title())
            .append(" (evidence: ").append(String.join(", ", entry.evidenceIds())).append(")\n"));

        builder.append("\n## Top RCA Findings\n");
        report.topFindings().forEach(finding -> builder
            .append("- ").append(finding.id()).append(": ").append(finding.hypothesis())
            .append(" Confidence: ").append(String.format("%.2f", finding.confidence()))
            .append(". Evidence: ").append(String.join(", ", finding.supportingEvidenceIds())).append("\n")
            .append("  - Uncertainty: ").append(finding.uncertainty()).append("\n"));

        return builder.toString();
    }
}
