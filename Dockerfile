FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=backend-build /workspace/target/*.jar app.jar

ENV PORT=8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"'

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]