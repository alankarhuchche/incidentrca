package com.example.incident.service;

import com.example.incident.domain.Evidence;
import com.example.incident.domain.TimelineEntry;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TimelineReconstructionService {

    public List<TimelineEntry> reconstruct(List<Evidence> evidence) {
        Map<String, List<Evidence>> grouped = evidence.stream()
            .collect(Collectors.groupingBy(Evidence::category));

        List<TimelineEntry> timeline = new ArrayList<>();

        grouped.forEach((category, evidenceList) -> {
            evidenceList.stream()
                .min(Comparator.comparing(Evidence::timestamp))
                .ifPresent(first -> timeline.add(new TimelineEntry(
                    first.timestamp(),
                    category + " observed",
                    "First " + category + " signal captured from synthetic observability feed",
                    evidenceList.stream().map(Evidence::id).sorted().toList()
                )));
        });

        return timeline.stream().sorted(Comparator.comparing(TimelineEntry::timestamp)).toList();
    }
}
