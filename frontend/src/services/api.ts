import type { AiExplanation, IncidentReport, IncidentSummary } from '../types';

export async function listIncidents(): Promise<IncidentSummary[]> {
  const response = await fetch('/api/incidents');
  if (!response.ok) throw new Error(`Failed to load incidents: ${response.status}`);
  return response.json() as Promise<IncidentSummary[]>;
}

export async function getExplanation(incidentId: string): Promise<AiExplanation> {
  const response = await fetch(`/api/incidents/${incidentId}/explain`);
  if (!response.ok) throw new Error(`Failed to load explanation: ${response.status}`);
  return response.json() as Promise<AiExplanation>;
}

export async function getIncidentReport(incidentId: string): Promise<IncidentReport> {
  const response = await fetch(`/api/incidents/${incidentId}`);
  if (!response.ok) {
    throw new Error(`Failed to load incident report: ${response.status}`);
  }
  return response.json() as Promise<IncidentReport>;
}
