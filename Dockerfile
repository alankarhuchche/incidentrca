# syntax=docker/dockerfile:1

FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/tsconfig.json frontend/tsconfig.app.json frontend/vite.config.ts frontend/index.html ./
COPY frontend/src ./src
RUN npm install && npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/META-INF/resources
RUN mvn -DskipTests -Dquarkus.package.jar.type=fast-jar package && \
    test -f target/quarkus-app/quarkus-run.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target ./target
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080} -jar /app/target/quarkus-app/quarkus-run.jar"]
