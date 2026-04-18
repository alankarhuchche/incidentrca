# Payments Incident RCA Copilot (MVP)

Public-safe, synthetic-data-only incident triage MVP for payment systems.

## What this first working version includes

- One end-to-end synthetic sample incident (`inc-2026-0042`)
- Timeline reconstruction from extracted evidence
- Evidence panel in UI
- Top 3 RCA findings with confidence + supporting evidence IDs
- Markdown report export endpoint
- Single Dockerfile for one Cloud Run deployment
- No external integrations
- No authentication (intentionally omitted for MVP)

## Monorepo layout

- `frontend/` React + TypeScript + Vite UI
- `backend/` Java 21 + Quarkus API and deterministic engines
- `backend/src/main/resources/data/` synthetic incident dataset
- `docs/architecture.md` architecture overview

## Local development

### Prerequisites

- Node.js 20+
- Java 21
- Maven 3.9+

### Frontend dev

```bash
cd frontend
npm install
npm run dev
```

### Backend dev

```bash
cd backend
mvn quarkus:dev
```

Open `http://localhost:8080` when running packaged mode with frontend assets copied in, or use Vite dev server for frontend-only iteration.

### Tests

```bash
cd backend
mvn test
```

### Build

```bash
cd frontend && npm install && npm run build
cd ../backend && mvn package
```

## API

- JSON report: `GET /api/incidents/inc-2026-0042`
- Markdown report: `GET /api/incidents/inc-2026-0042/report.md`

## Single-service Docker build

From repo root:

```bash
docker build -t incident-rca-copilot:local .
```

Run locally:

```bash
docker run --rm -p 8080:8080 incident-rca-copilot:local
```

## Cloud Run deployment

```bash
PROJECT_ID="your-gcp-project"
REGION="us-central1"
SERVICE="incident-rca-copilot"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE}:v1"

gcloud auth login
gcloud config set project "${PROJECT_ID}"
gcloud builds submit --tag "${IMAGE}"
gcloud run deploy "${SERVICE}" \
  --image "${IMAGE}" \
  --region "${REGION}" \
  --platform managed \
  --allow-unauthenticated \
  --port 8080
```

## Synthetic data policy

- This repository ships **synthetic events only**.
- Do not load real payment or customer data into this MVP.
- Human review is required before publishing any RCA.
