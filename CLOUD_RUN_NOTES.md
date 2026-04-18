Cloud Run demo deployment notes
Recommended demo deployment shape:
Single Cloud Run service
Quarkus backend serves the built React frontend as static assets
Synthetic incident datasets bundled in the container image
No external database or Kafka required for the first public demo
Why this shape:
Simpler for a novice
One URL to share on LinkedIn
Lower operational overhead
Faster first publish
Cloud Run packaging expectation for Codex:
Build frontend with Vite
Copy frontend dist assets into Quarkus static resources
Package backend and frontend into one container image
Expose HTTP on PORT expected by Cloud Run
Future enhancements after first publish:
split frontend and backend services
add Cloud SQL PostgreSQL
add Artifact Registry CI/CD and GitHub-triggered deploys
add authentication
add observability and structured audit persistence
