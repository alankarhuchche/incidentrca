import type { RcaFinding } from '../types';

type RcaFindingsPanelProps = {
  findings: RcaFinding[];
};

export function RcaFindingsPanel({ findings }: RcaFindingsPanelProps) {
  return (
    <section>
      <h2>Top 3 RCA Findings</h2>
      {findings.map((finding) => (
        <article key={finding.id}>
          <h3>{finding.id}</h3>
          <p>{finding.hypothesis}</p>
          <p>Confidence: {(finding.confidence * 100).toFixed(1)}%</p>
          <p>Supporting evidence: {finding.supportingEvidenceIds.join(', ')}</p>
          <p>Uncertainty: {finding.uncertainty}</p>
        </article>
      ))}
    </section>
  );
}
