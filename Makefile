.PHONY: frontend-dev backend-dev tests build full-build docker-build help

help:
	@echo "frontend-dev  start Vite dev server"
	@echo "backend-dev   start Quarkus dev server with hot reload"
	@echo "tests         run backend unit tests"
	@echo "build         build frontend and backend separately"
	@echo "full-build    build frontend, copy assets into backend, package backend jar"
	@echo "docker-build  build single-container Docker image"

frontend-dev:
	cd frontend && npm install && npm run dev

backend-dev:
	cd backend && mvn quarkus:dev

tests:
	cd backend && mvn test

build:
	cd frontend && npm install && npm run build
	cd backend && mvn package

full-build:
	cd frontend && npm install && npm run build
	cp -r frontend/dist/. backend/src/main/resources/META-INF/resources/
	cd backend && mvn package

docker-build:
	docker build -t incident-rca-copilot:local .
