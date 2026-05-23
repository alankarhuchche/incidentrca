import type { TimelineEntry } from '../types';

type TimelinePanelProps = {
  timeline: TimelineEntry[];
};

export function TimelinePanel({ timeline }: TimelinePanelProps) {
  return (
    <section>
      <h2>Timeline Reconstruction</h2>
      <ol>
        {timeline.map((entry) => (
          <li key={`${entry.timestamp}-${entry.title}`}>
            <strong>{new Date(entry.timestamp).toISOString().replace('T', ' ').slice(0, 19)} UTC</strong>
            <div>{entry.title}</div>
            <small>{entry.description}</small>
            <div>Evidence: {entry.evidenceIds.join(', ')}</div>
          </li>
        ))}
      </ol>
    </section>
  );
}
