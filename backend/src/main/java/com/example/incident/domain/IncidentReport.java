package com.example.incident.domain;

import java.util.List;

public record IncidentReport(
    String incidentId,
    String title,
    String status,
    String reviewRequirement,
    List<TimelineEntry> timeline,
    List<Evidence> evidence,
    List<RcaFinding> topFindings
) {}
