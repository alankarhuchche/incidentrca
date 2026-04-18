package com.example.incident.domain;

import java.time.Instant;
import java.util.Map;

public record IncidentEvent(
    String id,
    Instant timestamp,
    String source,
    String type,
    String service,
    String region,
    String severity,
    String message,
    Map<String, String> attributes
) {}
