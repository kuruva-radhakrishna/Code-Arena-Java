# Build stage: compile and package with the project's own Maven wrapper.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# Runtime stage: needs a full JDK, not just a JRE - this service invokes javac
# to compile submitted Java code, in addition to running itself. gcc/g++/python3
# are for compiling/running submitted C/C++/Python code.
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install --no-install-recommends -y gcc g++ python3 \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --create-home --shell /usr/sbin/nologin appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
