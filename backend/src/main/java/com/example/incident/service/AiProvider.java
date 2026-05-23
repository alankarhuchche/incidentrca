package com.example.incident.service;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.IncidentReport;

public interface AiProvider {
    AiExplanation explain(IncidentReport report);
}
