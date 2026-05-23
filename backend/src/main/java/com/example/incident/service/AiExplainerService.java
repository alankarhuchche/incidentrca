package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.IncidentReport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AiExplainerService {

    @ConfigProperty(name = "incident.ai.enabled", defaultValue = "false")
    boolean aiEnabled;

    @Inject
    NoopAiProvider noopProvider;

    @Inject
    GeminiAiProvider geminiProvider;

    public AiExplanation explain(IncidentReport report) {
        if (aiEnabled) return geminiProvider.explain(report);
        return noopProvider.explain(report);
    }
}
