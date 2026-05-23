# Payments Incident RCA Copilot (MVP)

Public-safe, synthetic-data-only incident triage MVP for payment systems.

## Live Demo

| | |
|---|---|
| **App** | https://incidentrca-638324583785.europe-west1.run.app/ |
| **Incident list API** | https://incidentrca-638324583785.europe-west1.run.app/api/incidents |
| **Health check** | https://incidentrca-638324583785.europe-west1.run.app/q/health/ready |

## What this first working version includes

- Two synthetic incidents (`inc-2026-0042`, `inc-2026-0099`) selectable via UI dropdown
- Timeline reconstruction from extracted evidence
- Evidence panel in UI
- Top RCA findings with confidence scores and supporting evidence IDs
- Evidence guard: findings without supporting evidence are suppressed
- Markdown report export endpoint
- AI explanation panel showing provider, mode badge, and amber safety warnings
- Optional Gemini AI integration (disabled by default); app safely falls back to deterministic explanation when the API key is absent
- Single Dockerfile for one Cloud Run deployment
- No authentication (intentionally omitted for MVP)

## Monorepo layout

- `frontend/` React + TypeScript + Vite UI
- `backend/` Java 21 + Quarkus API and deterministic engines
- `backend/src/main/resources/data/` synthetic incident datasets
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

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/incidents` | List all incidents (id, title, status) |
| `GET` | `/api/incidents/{id}` | Full JSON incident report |
| `GET` | `/api/incidents/{id}/report.md` | Markdown export |
| `GET` | `/api/incidents/{id}/explain` | AI or deterministic explanation |
| `GET` | `/q/health/ready` | Readiness probe |
| `GET` | `/q/health/live` | Liveness probe |

## AI explanations (optional)

Gemini AI is **disabled by default**. The app always produces a deterministic explanation first; AI is an optional layer on top.

To enable locally:

```bash
cd backend
INCIDENT_GEMINI_API_KEY=your-key INCIDENT_AI_ENABLED=true mvn quarkus:dev
```

If `INCIDENT_AI_ENABLED=true` but the API key is missing or blank, the `/explain` endpoint returns a safe deterministic explanation — it never throws a 500.

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
REGION="europe-west1"
SERVICE="incident-rca-copilot"
REPO="incident-rca"
IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}/${SERVICE}:v1"

gcloud auth login
gcloud config set project "${PROJECT_ID}"
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com secretmanager.googleapis.com

# Create Artifact Registry repository (one-time)
gcloud artifacts repositories create "${REPO}" \
  --repository-format=docker \
  --location="${REGION}"
```

### 2) Build and push container image using Cloud Build

```bash
gcloud builds submit --tag "${IMAGE}"
```

### 3) Deploy to Cloud Run

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

**Health check path:** Quarkus exposes `/q/health/ready` as the readiness probe. Configure this in the Cloud Run console under **Edit & Deploy → Health checks → Startup/Readiness probe path**. Some `gcloud` versions support `--health-check-path`; if yours does, add `--health-check-path=/q/health/ready` to the deploy command above.

### 4) Get and test the service URL

```bash
SERVICE_URL="$(gcloud run services describe "${SERVICE}" --region "${REGION}" --format='value(status.url)')"
echo "${SERVICE_URL}"
curl "${SERVICE_URL}/api/incidents"
curl "${SERVICE_URL}/q/health/ready"
```

### 5) Gemini AI on Cloud Run (optional)

Store the API key in Secret Manager so it never appears in environment variable history:

```bash
echo -n "your-gemini-api-key" | \
  gcloud secrets create gemini-api-key --data-file=-

# Grant the Cloud Run service account access
gcloud secrets add-iam-policy-binding gemini-api-key \
  --member="serviceAccount:YOUR_SERVICE_ACCOUNT@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

Then redeploy with the secret mounted as an environment variable:

```bash
gcloud run deploy "${SERVICE}" \
  --image "${IMAGE}" \
  --region "${REGION}" \
  --update-secrets="INCIDENT_GEMINI_API_KEY=gemini-api-key:latest" \
  --update-env-vars="INCIDENT_AI_ENABLED=true"
```

If the secret is unavailable or the key is blank, the app falls back to deterministic explanations automatically — no 500 errors.

### 6) Optional cost-saving mode (allow scale-to-zero)

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
