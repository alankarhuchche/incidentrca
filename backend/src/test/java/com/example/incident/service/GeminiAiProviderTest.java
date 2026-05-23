package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import com.example.incident.domain.TimelineEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GeminiAiProviderTest {

    private GeminiAiProvider providerWithNoKey() throws Exception {
        GeminiAiProvider provider = new GeminiAiProvider();
        Field f = GeminiAiProvider.class.getDeclaredField("apiKey");
        f.setAccessible(true);
        f.set(provider, Optional.empty());
        return provider;
    }

    private IncidentReport sampleReport() {
        var timeline = List.of(new TimelineEntry(
            Instant.parse("2026-04-17T09:00:12Z"),
            "db_error observed", "First db_error signal", List.of("E-evt-001")
        ));
        var evidence = List.of(new Evidence(
            "E-evt-001", "db_error", "lock wait timeout", "evt-001",
            Instant.parse("2026-04-17T09:00:12Z"), "service=payments-db"
        ));
        var findings = List.of(new RcaFinding(
            "RCA-1", "Primary key hot partition caused payment timeout.",
            0.78, "Requires human verification.", List.of("E-evt-001")
        ));
        return new IncidentReport(
            "inc-2026-0042", "Synthetic payment degradation", "Mitigated",
            "Human review is mandatory before final RCA publication.",
            timeline, evidence, findings
        );
    }

    @Test
    void explainReturnsSafeExplanationWhenApiKeyIsAbsent() throws Exception {
        GeminiAiProvider provider = providerWithNoKey();
        AiExplanation result = provider.explain(sampleReport());

        assertEquals("gemini", result.provider());
        assertFalse(result.aiGenerated());
        assertTrue(result.summary().contains("not configured"),
            "Summary should explain that the API key is not configured; got: " + result.summary());
    }
}
