package com.example.incident.service;

import com.example.incident.domain.IncidentDataset;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@ApplicationScoped
public class SyntheticIncidentRepository {

    @Inject
    ObjectMapper objectMapper;

    public IncidentDataset load(String incidentId) {
        if (!"inc-2026-0042".equals(incidentId)) {
            return null;
        }

        try (InputStream input = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("data/incident-inc-2026-0042.json")) {
            if (input == null) {
                throw new IllegalStateException("Synthetic dataset missing for incident " + incidentId);
            }
            return objectMapper.readValue(input, IncidentDataset.class);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read synthetic incident dataset", exception);
        }
    }
}
