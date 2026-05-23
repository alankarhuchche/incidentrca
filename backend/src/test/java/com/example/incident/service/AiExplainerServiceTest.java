package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.RcaFinding;
import com.example.incident.domain.TimelineEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiExplainerServiceTest {

    private AiExplainerService service;
    private NoopAiProvider noopProvider;
    private GeminiAiProvider geminiProvider;

    @BeforeEach
    void setUp() throws Exception {
        noopProvider = new NoopAiProvider();
        geminiProvider = mock(GeminiAiProvider.class);
        service = new AiExplainerService();
        setField("noopProvider", noopProvider);
        setField("geminiProvider", geminiProvider);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = AiExplainerService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private void setAiEnabled(boolean value) throws Exception {
        Field f = AiExplainerService.class.getDeclaredField("aiEnabled");
        f.setAccessible(true);
        f.setBoolean(service, value);
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
    void explainUsesNoopWhenAiDisabled() throws Exception {
        setAiEnabled(false);
        AiExplanation result = service.explain(sampleReport());
        assertEquals("noop", result.provider());
    }

    @Test
    void explainUsesGeminiWhenAiEnabled() throws Exception {
        setAiEnabled(true);
        AiExplanation fake = new AiExplanation(
            "inc-2026-0042", "Gemini summary.", "Root cause.", "Review required.", "gemini", true
        );
        when(geminiProvider.explain(any())).thenReturn(fake);

        AiExplanation result = service.explain(sampleReport());
        assertEquals("gemini", result.provider());
        verify(geminiProvider).explain(any());
    }

    @Test
    void explainDoesNotCallGeminiWhenAiDisabled() throws Exception {
        setAiEnabled(false);
        service.explain(sampleReport());
        verifyNoInteractions(geminiProvider);
    }
}
