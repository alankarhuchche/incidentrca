import type { IncidentReport } from '../types';

export async function getIncidentReport(incidentId: string): Promise<IncidentReport> {
  const response = await fetch(`/api/incidents/${incidentId}`);
  if (!response.ok) {
    throw new Error(`Failed to load incident report: ${response.status}`);
  }
  return response.json() as Promise<IncidentReport>;
}
