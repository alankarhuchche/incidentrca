export type TimelineEntry = {
  timestamp: string;
  title: string;
  description: string;
  evidenceIds: string[];
};

export type Evidence = {
  id: string;
  category: string;
  summary: string;
  sourceEventId: string;
  timestamp: string;
  detail: string;
};

export type RcaFinding = {
  id: string;
  hypothesis: string;
  confidence: number;
  uncertainty: string;
  supportingEvidenceIds: string[];
};

export type IncidentReport = {
  incidentId: string;
  title: string;
  status: string;
  reviewRequirement: string;
  timeline: TimelineEntry[];
  evidence: Evidence[];
  topFindings: RcaFinding[];
};
