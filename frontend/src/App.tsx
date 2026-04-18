import { useEffect, useState } from 'react';
import { EvidencePanel } from './components/EvidencePanel';
import { RcaFindingsPanel } from './components/RcaFindingsPanel';
import { TimelinePanel } from './components/TimelinePanel';
import { getIncidentReport } from './services/api';
import type { IncidentReport } from './types';
import './app.css';

const SAMPLE_INCIDENT_ID = 'inc-2026-0042';

function App() {
  const [report, setReport] = useState<IncidentReport | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getIncidentReport(SAMPLE_INCIDENT_ID)
      .then(setReport)
      .catch((loadError) => setError(loadError instanceof Error ? loadError.message : 'Unknown error'));
  }, []);

  return (
    <main>
      <h1>Payments Incident RCA Copilot</h1>
      <p>Using synthetic incident data only. Human review required before final report publication.</p>
      {error && <p role="alert">{error}</p>}
      {!report && !error && <p>Loading sample incident...</p>}
      {report && (
        <>
          <section>
            <h2>{report.title}</h2>
            <p>Incident ID: {report.incidentId}</p>
            <p>Status: {report.status}</p>
            <p>{report.reviewRequirement}</p>
            <a href={`/api/incidents/${report.incidentId}/report.md`} target="_blank" rel="noreferrer">
              Export Markdown Report
            </a>
          </section>
          <TimelinePanel timeline={report.timeline} />
          <EvidencePanel evidence={report.evidence} />
          <RcaFindingsPanel findings={report.topFindings} />
        </>
      )}
    </main>
  );
}

export default App;
