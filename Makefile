.PHONY: frontend-dev backend-dev tests build docker-build

frontend-dev:
	cd frontend && npm install && npm run dev

backend-dev:
	cd backend && mvn quarkus:dev

tests:
	cd backend && mvn test

build:
	cd frontend && npm install && npm run build
	cd backend && mvn package

docker-build:
	docker build -t incident-rca-copilot:local .
