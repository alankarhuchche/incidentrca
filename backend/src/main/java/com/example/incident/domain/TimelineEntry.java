package com.example.incident.domain;

import java.time.Instant;
import java.util.List;

public record TimelineEntry(
    Instant timestamp,
    String title,
    String description,
    List<String> evidenceIds
) {}
