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
    test -d target

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target ./target
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "if [ -f /app/target/quarkus-app/quarkus-run.jar ]; then exec java -Dquarkus.http.host=0.0.0.0 -jar /app/target/quarkus-app/quarkus-run.jar; fi; RUNNER=$(find /app/target -maxdepth 1 -name '*-runner.jar' | head -n 1); if [ -n \"$RUNNER\" ]; then exec java -Dquarkus.http.host=0.0.0.0 -jar \"$RUNNER\"; fi; APP_JAR=$(find /app/target -maxdepth 1 -name '*.jar' ! -name 'original-*.jar' | head -n 1); if [ -n \"$APP_JAR\" ]; then exec java -Dquarkus.http.host=0.0.0.0 -jar \"$APP_JAR\"; fi; echo 'No runnable JAR found under /app/target'; find /app/target -maxdepth 3 -type f; exit 1"]
