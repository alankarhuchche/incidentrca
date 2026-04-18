package com.example.incident.domain;

import java.time.Instant;

public record Evidence(
    String id,
    String category,
    String summary,
    String sourceEventId,
    Instant timestamp,
    String detail
) {}
