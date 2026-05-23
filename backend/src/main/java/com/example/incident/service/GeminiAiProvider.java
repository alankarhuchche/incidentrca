package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class GeminiAiProvider implements AiProvider {

    @ConfigProperty(name = "incident.gemini.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "incident.gemini.model", defaultValue = "gemini-2.5-flash")
    String model;

    private Client client;

    private synchronized Client getClient() {
        if (client == null) {
            client = new Client.Builder().apiKey(apiKey.get()).build();
        }
        return client;
    }

    @Override
    public AiExplanation explain(IncidentReport report) {
        if (apiKey.isEmpty() || apiKey.get().isBlank()) {
            return missingKeyExplanation(report);
        }

        String prompt = buildPrompt(report);
        GenerateContentResponse response = getClient().models.generateContent(model, prompt, null);
        String text = response.text();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException(
                "Gemini returned empty response for incident " + report.incidentId());
        }

        String rootCause = report.topFindings().isEmpty()
            ? "No root cause identified."
            : report.topFindings().get(0).hypothesis();

        return new AiExplanation(
            report.incidentId(), text, rootCause,
            report.reviewRequirement(), "gemini", true
        );
    }

    private AiExplanation missingKeyExplanation(IncidentReport report) {
        String rootCause = report.topFindings().isEmpty()
            ? "No root cause identified."
            : report.topFindings().get(0).hypothesis();
        return new AiExplanation(
            report.incidentId(),
            "Gemini is enabled (incident.ai.enabled=true) but the API key is not configured. " +
            "Set INCIDENT_GEMINI_API_KEY to enable AI-generated explanations.",
            rootCause,
            report.reviewRequirement(),
            "gemini",
            false
        );
    }

    private String buildPrompt(IncidentReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an incident analysis assistant for a payments engineering team.\n");
        sb.append("Analyze this incident and provide a concise 2-3 sentence explanation.\n\n");
        sb.append("Incident: ").append(report.incidentId())
            .append(" — ").append(report.title()).append("\n");
        sb.append("Status: ").append(report.status()).append("\n\n");

        if (!report.topFindings().isEmpty()) {
            sb.append("Top RCA findings (ranked by confidence):\n");
            for (RcaFinding f : report.topFindings()) {
                sb.append("  ").append(f.id()).append(": ").append(f.hypothesis())
                    .append(" (").append(String.format("%.0f%%", f.confidence() * 100)).append(")\n");
            }
            sb.append("\n");
        }

        if (!report.timeline().isEmpty()) {
            sb.append("Timeline: ")
                .append(report.timeline().get(0).timestamp())
                .append(" → ")
                .append(report.timeline().get(report.timeline().size() - 1).timestamp())
                .append("\n\n");
        }

        sb.append("Explain what happened, the most likely root cause, and any key uncertainty. ");
        sb.append("Do not reproduce synthetic data labels. ");
        sb.append("Human review is required before publishing this analysis.");
        return sb.toString();
    }
}
