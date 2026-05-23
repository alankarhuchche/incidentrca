package com.example.incident.service;

import com.example.incident.domain.IncidentDataset;
import com.example.incident.domain.IncidentSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
public class SyntheticIncidentRepository {

    private static final Set<String> KNOWN_IDS = Set.of("inc-2026-0042", "inc-2026-0099");

    @Inject
    ObjectMapper objectMapper;

    public IncidentDataset load(String incidentId) {
        if (!KNOWN_IDS.contains(incidentId)) {
            return null;
        }

        try (InputStream input = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("data/incident-" + incidentId + ".json")) {
            if (input == null) {
                throw new IllegalStateException("Synthetic dataset missing for incident " + incidentId);
            }
            return objectMapper.readValue(input, IncidentDataset.class);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read synthetic incident dataset", exception);
        }
    }

    public List<IncidentSummary> listSummaries() {
        return KNOWN_IDS.stream()
            .sorted()
            .map(this::load)
            .filter(Objects::nonNull)
            .map(d -> new IncidentSummary(d.incidentId(), d.title(), d.status()))
            .toList();
    }
}
