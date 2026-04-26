# External Critical Review (Pre-Publication)

## Scope reviewed

- Product plan and claims in `README.md`, `docs/architecture.md`, and `CLOUD_RUN_NOTES.md`.
- Backend domain, API, repository, extraction, timeline, ranking, export, and tests.
- Frontend workflow and rendering of incident report data.

## Executive assessment

The MVP is directionally strong for a synthetic-data RCA demo, but an external reviewer will likely flag it as **prototype-grade** rather than commercial-grade due to hardcoded incident handling, limited test coverage, non-portable time rendering, simplistic ranking logic, and missing production controls (auth, rate limits, audit trail, and contract validation).

## Where the current plan fails against critical external scrutiny

### 1) "Commercial-grade" claim is ahead of implementation

- The app intentionally ships without authentication, which is documented, but this directly conflicts with enterprise-readiness signaling for an internet-exposed service.
- Deployment notes still frame security and observability as future work rather than minimum publish bar.
- Only one synthetic incident is supported in the repository; this undermines confidence in generalization.

### 2) Deterministic evidence vs. inference boundary is conceptually right but thinly enforced

- The architecture and service separation are good.
- However, findings are generated from three fixed hypotheses with category-count-derived confidence and no explicit falsification rules.
- This makes output predictable for demo data but vulnerable to criticism as "pre-baked conclusions" instead of robust hypothesis generation.

### 3) Traceability is present but weakly constrained

- Findings include supporting evidence IDs, which is good.
- There is no validation preventing empty evidence references from being returned in edge cases.
- There is no schema or policy check proving every finding maps to evidence from the same incident in all paths.

### 4) Reproducibility and portability risk in UI

- Frontend timeline and evidence timestamps are rendered using `toLocaleString()`, causing reviewer-visible time differences by browser locale and timezone.
- Inconsistent displayed times can weaken trust in incident chronology during demos.

### 5) Operational credibility gaps for public exposure

- No auth, no request throttling, no CORS policy hardening, no structured audit events for report access.
- No health/readiness endpoint strategy documented for production SLO monitoring.
- No explicit API versioning strategy (`/api/v1/...`) for compatibility once public users depend on this.

### 6) Test strategy is underpowered for claims

- Tests only cover ranking order and timeline grouping at unit level.
- Missing tests for repository loading behavior, API contract behavior (including 404 and markdown export), and regression tests for evidence/finding linkage invariants.
- No frontend tests for critical rendering paths or error states.

## Concrete code shortcomings an external reviewer may call out

1. **Single-incident hardcoding** in repository loading (`inc-2026-0042` only), limiting realism and extensibility.
2. **Ranking model fragility**: confidence is linear-by-count and capped; event severity, source quality, and temporal proximity are ignored.
3. **Timeline collapse by category**: timeline preserves only first-seen event per category, potentially hiding important progression.
4. **Markdown export safety/consistency**: raw text interpolation could produce malformed markdown when messages include markdown control characters.
5. **Error ergonomics**: API returns minimal error details and frontend exposes status-code-based message only.
6. **No explicit DTO/API schema versioning** for external consumers.

## What a strict external reviewer is likely to do

1. Attempt invalid and unknown incident IDs and check error model consistency.
2. Compare JSON and markdown outputs for mismatch/drift.
3. Inject unusual synthetic event strings and verify markdown/export stability.
4. Validate whether confidence values are calibrated or merely deterministic placeholders.
5. Check whether evidence IDs in findings are always non-empty and incident-local.
6. Probe API availability, abuse resistance, and unauthenticated exposure concerns.
7. Ask for evidence that demo claims map to automated tests.

## Priority remediation plan (publish gate)

### P0 (before broad internet publish)

- Add minimal authentication or access gating for public endpoint use.
- Add request rate limiting and basic structured audit logging for report access.
- Add API contract tests for success + not-found + markdown export parity.
- Render timestamps in canonical UTC in UI and exports.
- Add invariant checks: each finding must carry at least one valid evidence ID from same incident.

### P1 (immediately after first publish)

- Support multiple synthetic incidents and incident listing endpoint.
- Upgrade ranking to weighted signals (severity, recency, source confidence) with explicit rationale.
- Expand timeline reconstruction beyond "first event per category".
- Add frontend tests for loading/error/report render.

### P2 (commercial hardening path)

- Versioned API (`/api/v1`) and backward-compatibility policy.
- Persisted synthetic store abstraction with explicit repository interface.
- Stronger observability: request IDs, metrics, trace correlation, audit retention policy.
- Security review checklist in repo (threat model, abuse cases, operational runbook).

## Suggested external-facing positioning

If publishing now, position this explicitly as:

- "Synthetic-data-only deterministic RCA workflow demo"
- "Not production security-complete"
- "Designed to demonstrate evidence traceability and human-review-first process"

This framing reduces risk of over-claiming and aligns public expectations with current implementation maturity.
