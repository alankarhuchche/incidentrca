AGENTS.md
Project
Payments Incident RCA Copilot
Purpose
A public-safe, commercial-grade MVP for incident triage in payment systems using synthetic data only.
Repo expectations
Frontend: React + TypeScript + Vite
Backend: Java 21 + Quarkus
Database: PostgreSQL later; for the first public demo, use synthetic files and in-memory storage unless explicitly asked to wire Cloud SQL.
Mock event and observability providers
Synthetic incident datasets in the repo
Strong separation between deterministic evidence extraction and AI narrative generation
Engineering principles
Do not use real payment or customer data.
Never mix evidence extraction with narrative generation.
Every inferred RCA must reference supporting evidence IDs.
Human review is mandatory.
No auto-remediation in MVP.
Avoid fake certainty. Prefer explicit uncertainty over overconfident guesses.
Keep architecture commercially credible and ready for enterprise hardening.
Coding rules
Keep domain models explicit and typed.
Prefer small modules over large utility files.
Backend owns business logic.
Frontend owns workflow, presentation, and interaction.
Use testable services and interfaces.
Add tests for correlation and ranking logic.
No dead code or placeholder comments like "TODO later" without context.
Build and run
Codex must keep these commands working:
frontend dev
backend dev
tests
build
docker build
Deliverables
working monorepo
synthetic datasets
incident workflow UI
correlation engine
RCA ranking engine
markdown and JSON report export
README
architecture doc
Dockerfile for Cloud Run demo deployment
Review checklist
Before considering a task complete:
Does this change preserve public-safe synthetic data only?
Is evidence separated from inference?
Are conclusions traceable?
Are types and interfaces clean?
Are tests updated?
Does the app still run?
