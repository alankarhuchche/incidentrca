package com.example.incident.domain;

import java.util.List;

public record IncidentDataset(
    String incidentId,
    String title,
    String status,
    List<IncidentEvent> events
) {}
