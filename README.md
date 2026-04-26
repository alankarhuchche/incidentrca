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

## Cloud Run deployment (step-by-step)

This repo is ready for a **single Cloud Run service** deployment where Quarkus serves both API + built frontend assets from one URL.

### 1) One-time setup

```bash
PROJECT_ID="your-gcp-project"
REGION="us-central1"
SERVICE="incident-rca-copilot"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE}:v1"

gcloud auth login
gcloud config set project "${PROJECT_ID}"
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com
```

### 2) Build and push container image using Cloud Build

```bash
gcloud builds submit --tag "${IMAGE}"
```

### 3) Deploy to Cloud Run with internet access

```bash
gcloud run deploy "${SERVICE}" \
  --image "${IMAGE}" \
  --region "${REGION}" \
  --platform managed \
  --allow-unauthenticated \
  --port 8080 \
  --min-instances 1 \
  --max-instances 3 \
  --memory 512Mi \
  --cpu 1 \
  --concurrency 80 \
  --timeout 300
```

### 4) Get and test the service URL

```bash
SERVICE_URL="$(gcloud run services describe "${SERVICE}" --region "${REGION}" --format='value(status.url)')"
echo "${SERVICE_URL}"
curl "${SERVICE_URL}/api/incidents/inc-2026-0042"
```

### 5) Optional cost-saving mode (allow scale-to-zero)

Use this if you do not need the app always warm:

```bash
gcloud run services update "${SERVICE}" --region "${REGION}" --min-instances 0
```

### Troubleshooting Cloud Build failures

If Cloud Build fails, fetch the latest build details:

```bash
gcloud builds list --limit=5
gcloud builds log --stream BUILD_ID
```

Common causes and fixes in this repo:

- **Large source upload / timeout** from local artifacts (`node_modules`, `dist`, `target`):
  this repo now includes `.gcloudignore` and `.dockerignore` to exclude those directories.
- **Permission errors**: confirm `run.googleapis.com`, `cloudbuild.googleapis.com`, and
  `artifactregistry.googleapis.com` are enabled and your account has deploy permissions.
- **`COPY --from=backend-build /app/backend/target/quarkus-app` not found**:
  this repo's Dockerfile now copies the full backend `target/` directory and
  auto-detects a runnable artifact at startup (`quarkus-app/quarkus-run.jar`,
  `*-runner.jar`, or a top-level `*.jar`) to avoid hard-failing on one layout.
- **Quarkus package layout unexpected / no `quarkus-app` output**:
  the backend Maven plugin is explicitly configured as `io.quarkus:quarkus-maven-plugin`
  so Quarkus packaging goals are correctly wired during `mvn package`.

## Synthetic data policy

- This repository ships **synthetic events only**.
- Do not load real payment or customer data into this MVP.
- Human review is required before publishing any RCA.
