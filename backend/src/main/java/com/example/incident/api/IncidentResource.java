package com.example.incident.api;

import com.example.incident.domain.AiExplanation;
import com.example.incident.domain.IncidentReport;
import com.example.incident.domain.IncidentSummary;
import com.example.incident.service.AiExplainerService;
import com.example.incident.service.IncidentReportService;
import com.example.incident.service.ReportExportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/incidents")
public class IncidentResource {

    @Inject
    IncidentReportService reportService;

    @Inject
    ReportExportService exportService;

    @Inject
    AiExplainerService aiExplainerService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<IncidentSummary> listIncidents() {
        return reportService.listIncidents();
    }

    @GET
    @Path("/{incidentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public IncidentReport getReport(@PathParam("incidentId") String incidentId) {
        IncidentReport report = reportService.buildReport(incidentId);
        if (report == null) {
            throw new NotFoundException("Incident not found: " + incidentId);
        }
        return report;
    }

    @GET
    @Path("/{incidentId}/report.md")
    @Produces("text/markdown")
    public String exportMarkdown(@PathParam("incidentId") String incidentId) {
        IncidentReport report = getReport(incidentId);
        return exportService.toMarkdown(report);
    }

    @GET
    @Path("/{incidentId}/explain")
    @Produces(MediaType.APPLICATION_JSON)
    public AiExplanation explainIncident(@PathParam("incidentId") String incidentId) {
        IncidentReport report = reportService.buildReport(incidentId);
        if (report == null) {
            throw new NotFoundException("Incident not found: " + incidentId);
        }
        return aiExplainerService.explain(report);
    }
}
