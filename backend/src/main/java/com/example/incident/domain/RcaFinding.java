package com.example.incident.domain;

import java.util.List;

public record RcaFinding(
    String id,
    String hypothesis,
    double confidence,
    String uncertainty,
    List<String> supportingEvidenceIds
) {}
