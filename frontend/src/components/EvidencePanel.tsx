import type { Evidence } from '../types';

type EvidencePanelProps = {
  evidence: Evidence[];
};

export function EvidencePanel({ evidence }: EvidencePanelProps) {
  return (
    <section>
      <h2>Evidence Panel</h2>
      <table>
        <thead>
          <tr>
            <th>Evidence ID</th>
            <th>Timestamp</th>
            <th>Category</th>
            <th>Summary</th>
            <th>Detail</th>
          </tr>
        </thead>
        <tbody>
          {evidence.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{new Date(item.timestamp).toLocaleString()}</td>
              <td>{item.category}</td>
              <td>{item.summary}</td>
              <td>{item.detail}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
