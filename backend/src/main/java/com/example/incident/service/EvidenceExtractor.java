package com.example.incident.service;

import com.example.incident.domain.Evidence;
import com.example.incident.domain.IncidentEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class EvidenceExtractor {

    public List<Evidence> extract(List<IncidentEvent> events) {
        List<Evidence> evidence = new ArrayList<>();

        events.stream()
            .sorted(Comparator.comparing(IncidentEvent::timestamp))
            .forEach(event -> evidence.add(new Evidence(
                "E-" + event.id(),
                event.type(),
                event.message(),
                event.id(),
                event.timestamp(),
                "service=" + event.service() + ", region=" + event.region() + ", severity=" + event.severity()
            )));

        return evidence;
    }
}
