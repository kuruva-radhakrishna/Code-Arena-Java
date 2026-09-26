# Build stage: compile and package with the project's own Maven wrapper (the
# same repo.maven.apache.org-hosted distribution CI uses), skipping tests -
# they've already run in CI; this stage just needs to produce the jar.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# Runtime stage: JRE only, no build tools.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home --shell /usr/sbin/nologin appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
