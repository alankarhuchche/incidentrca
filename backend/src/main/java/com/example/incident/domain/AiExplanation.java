package com.example.incident.domain;

public record AiExplanation(
    String incidentId,
    String summary,
    String rootCauseSummary,
    String recommendedAction,
    String provider,
    boolean aiGenerated
) {}
