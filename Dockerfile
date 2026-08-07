FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY target/onboarding-client-g-0.1.0-SNAPSHOT.jar application.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
