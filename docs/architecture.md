# Architecture Overview

## Deployment shape

This MVP uses a **single Cloud Run service**:

1. React + Vite frontend is built to static assets.
2. Quarkus serves those assets from `META-INF/resources`.
3. Quarkus also exposes JSON and Markdown incident report APIs.

This gives one URL, no external dependencies, and synthetic-data-only operation.

## Core modules

- `SyntheticIncidentRepository`: loads one synthetic incident from bundled JSON.
- `EvidenceExtractor`: deterministic conversion from raw events to evidence records.
- `TimelineReconstructionService`: deterministic timeline reconstruction grouped and ordered by category and first-seen timestamp.
- `RcaRankingService`: deterministic RCA scoring/ranking and top-3 selection.
- `ReportExportService`: markdown export only; no external integrations.

## Data safety and inference boundaries

- All source events are synthetic and stored in-repo.
- Evidence extraction is deterministic and separate from findings ranking.
- Every RCA finding includes supporting evidence IDs.
- Every report includes explicit uncertainty and a mandatory human review requirement.
