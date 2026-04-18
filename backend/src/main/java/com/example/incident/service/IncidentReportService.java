package com.example.incident.service;

import com.example.incident.domain.IncidentDataset;
import com.example.incident.domain.IncidentReport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IncidentReportService {

    @Inject
    SyntheticIncidentRepository repository;

    @Inject
    EvidenceExtractor evidenceExtractor;

    @Inject
    TimelineReconstructionService timelineService;

    @Inject
    RcaRankingService rankingService;

    public IncidentReport buildReport(String incidentId) {
        IncidentDataset incident = repository.load(incidentId);
        if (incident == null) {
            return null;
        }

        var evidence = evidenceExtractor.extract(incident.events());
        var timeline = timelineService.reconstruct(evidence);
        var findings = rankingService.rankTopFindings(evidence);

        return new IncidentReport(
            incident.incidentId(),
            incident.title(),
            incident.status(),
            "Human review is mandatory before final RCA publication.",
            timeline,
            evidence,
            findings
        );
    }
}
