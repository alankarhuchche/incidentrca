package com.example.incident.service;

import com.example.incident.domain.Evidence;
import com.example.incident.domain.RcaFinding;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class RcaRankingService {

    public List<RcaFinding> rankTopFindings(List<Evidence> evidence) {
        Map<String, List<Evidence>> byCategory = evidence.stream()
            .collect(Collectors.groupingBy(Evidence::category));

        List<RcaFinding> findings = new ArrayList<>();

        findings.add(buildDatabaseHypothesis(byCategory.getOrDefault("db_error", List.of())));
        findings.add(buildGatewayHypothesis(byCategory.getOrDefault("payment_gateway", List.of())));
        findings.add(buildRetryHypothesis(byCategory.getOrDefault("application_log", List.of())));

        return findings.stream()
            .filter(f -> !f.supportingEvidenceIds().isEmpty())
            .sorted(Comparator.comparing(RcaFinding::confidence).reversed())
            .limit(3)
            .toList();
    }

    private RcaFinding buildDatabaseHypothesis(List<Evidence> evidence) {
        double confidence = evidence.isEmpty() ? 0.35 : Math.min(0.95, 0.62 + evidence.size() * 0.08);
        return new RcaFinding(
            "RCA-1",
            "Primary key hot partition on payments_db elevated lock wait time and caused payment timeout propagation.",
            confidence,
            "Confidence decreases if lock metrics are delayed; requires human verification before sign-off.",
            evidence.stream().map(Evidence::id).limit(4).toList()
        );
    }

    private RcaFinding buildGatewayHypothesis(List<Evidence> evidence) {
        double confidence = evidence.isEmpty() ? 0.30 : Math.min(0.88, 0.54 + evidence.size() * 0.07);
        return new RcaFinding(
            "RCA-2",
            "Gateway timeout surge is likely secondary impact from upstream database contention rather than provider outage.",
            confidence,
            "Could be under-estimated if external gateway telemetry is incomplete in synthetic stream.",
            evidence.stream().map(Evidence::id).limit(4).toList()
        );
    }

    private RcaFinding buildRetryHypothesis(List<Evidence> evidence) {
        double confidence = evidence.isEmpty() ? 0.25 : Math.min(0.80, 0.45 + evidence.size() * 0.06);
        return new RcaFinding(
            "RCA-3",
            "Retry storm from checkout-service amplified queue depth and prolonged customer-visible latency.",
            confidence,
            "Needs operator confirmation that retry backoff policy was unchanged during incident window.",
            evidence.stream().map(Evidence::id).limit(4).toList()
        );
    }
}
