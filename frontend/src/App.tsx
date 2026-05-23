import { useEffect, useState } from 'react';
import { EvidencePanel } from './components/EvidencePanel';
import { RcaFindingsPanel } from './components/RcaFindingsPanel';
import { TimelinePanel } from './components/TimelinePanel';
import { listIncidents, getIncidentReport } from './services/api';
import type { IncidentReport, IncidentSummary } from './types';
import './app.css';

function App() {
  const [incidents, setIncidents] = useState<IncidentSummary[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [report, setReport] = useState<IncidentReport | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listIncidents()
      .then((list) => {
        setIncidents(list);
        if (list.length > 0) setSelectedId(list[0].id);
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load incidents'));
  }, []);

  useEffect(() => {
    if (!selectedId) return;
    setLoading(true);
    setReport(null);
    getIncidentReport(selectedId)
      .then((r) => { setReport(r); setLoading(false); })
      .catch((e) => { setError(e instanceof Error ? e.message : 'Unknown error'); setLoading(false); });
  }, [selectedId]);

  return (
    <main>
      <h1>Payments Incident RCA Copilot</h1>
      <p>Using synthetic incident data only. Human review required before final report publication.</p>
      {incidents.length > 0 && (
        <section>
          <label htmlFor="incident-select">Incident: </label>
          <select
            id="incident-select"
            value={selectedId ?? ''}
            onChange={(e) => { setError(null); setSelectedId(e.target.value); }}
          >
            {incidents.map((inc) => (
              <option key={inc.id} value={inc.id}>
                {inc.id} — {inc.title}
              </option>
            ))}
          </select>
        </section>
      )}
      {error && <p role="alert">{error}</p>}
      {loading && <p>Loading incident...</p>}
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
