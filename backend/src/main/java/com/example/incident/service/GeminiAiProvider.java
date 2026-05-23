package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import com.example.incident.domain.TimelineEntry;
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

        sb.append("You are an incident review assistant for a regulated payments engineering organisation.\n");
        sb.append("Your audience includes engineers, technical leads, and governance stakeholders.\n");
        sb.append("The deterministic RCA engine has already identified the root cause and supporting evidence.\n");
        sb.append("Do not merely repeat the raw technical root cause. Use the deterministic RCA finding as input, ");
        sb.append("then explain its impact, risk implications, operational consequences and follow-up actions in plain English.\n\n");

        sb.append("=== INCIDENT ===\n");
        sb.append("ID: ").append(report.incidentId()).append("\n");
        sb.append("Title: ").append(report.title()).append("\n");
        sb.append("Status: ").append(report.status()).append("\n\n");

        if (!report.timeline().isEmpty()) {
            sb.append("=== TIMELINE ===\n");
            for (TimelineEntry t : report.timeline()) {
                sb.append(t.timestamp()).append("  ")
                  .append(t.title()).append(": ").append(t.description()).append("\n");
            }
            sb.append("\n");
        }

        if (!report.evidence().isEmpty()) {
            sb.append("=== EVIDENCE ===\n");
            for (Evidence e : report.evidence()) {
                sb.append("[").append(e.id()).append("] ")
                  .append(e.category()).append(": ").append(e.summary()).append("\n");
            }
            sb.append("\n");
        }

        if (!report.topFindings().isEmpty()) {
            sb.append("=== RCA FINDINGS (ranked by confidence) ===\n");
            for (RcaFinding f : report.topFindings()) {
                sb.append(f.id()).append(": ").append(f.hypothesis())
                  .append(" (confidence: ")
                  .append(String.format("%.0f%%", f.confidence() * 100)).append(")\n");
                sb.append("  Uncertainty: ").append(f.uncertainty()).append("\n");
                sb.append("  Supporting evidence: ")
                  .append(String.join(", ", f.supportingEvidenceIds())).append("\n");
            }
            sb.append("\n");
        }

        sb.append("=== YOUR TASK ===\n");
        sb.append("Write a structured incident review in plain English.\n");
        sb.append("Use exactly these section labels, each on its own line, followed by the content.\n");
        sb.append("Do not use markdown headers (no ## or **). Plain text only.\n\n");

        sb.append("EXECUTIVE SUMMARY\n");
        sb.append("2-3 sentences. What happened, when, and what was the business effect. No jargon.\n\n");

        sb.append("CUSTOMER AND BUSINESS IMPACT\n");
        sb.append("Explain confirmed customer or business impact from the supplied incident data. ");
        sb.append("If the impact is inferred, label it clearly as inferred. ");
        sb.append("If the evidence does not provide scope, say that scope is unknown.\n\n");

        sb.append("TECHNICAL EXPLANATION\n");
        sb.append("What failed and why, in plain English for a senior engineering manager, not a database specialist.\n\n");

        sb.append("OPERATIONAL IMPACT\n");
        sb.append("Effect on on-call teams, SLA/SLO position, escalation path, duration of degraded state.\n\n");

        sb.append("RISK AND CONTROL IMPLICATIONS\n");
        sb.append("What risk or control gap does this expose? First occurrence or recurrence? Any compliance relevance?\n\n");

        sb.append("CORRECTIVE ACTIONS\n");
        sb.append("Specific, assignable actions to fully remediate this incident. Use action-verb format.\n\n");

        sb.append("PREVENTIVE ACTIONS\n");
        sb.append("Longer-term changes to prevent recurrence. Separate from corrective actions.\n\n");

        sb.append("QUESTIONS FOR INCIDENT REVIEW\n");
        sb.append("3-5 open questions for the review board. Focus on systemic issues, not blame.\n\n");

        sb.append("MONITORING RECOMMENDATIONS\n");
        sb.append("Specific alerts, dashboards, or metrics that would have detected this earlier or reduced MTTR.\n\n");

        sb.append("SOURCE OF TRUTH REMINDER\n");
        sb.append("State clearly that this AI-generated review is supplementary. ");
        sb.append("The deterministic RCA findings and their supporting evidence IDs are the evidence-backed source of truth ");
        sb.append("and must be reviewed by a qualified engineer before any action is taken.\n");

        return sb.toString();
    }
}
