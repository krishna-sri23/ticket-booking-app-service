# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy wrapper + pom first (cached layer if pom unchanged)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Pre-fetch dependencies for caching; non-fatal if network is flaky (package step will retry)
RUN ./mvnw dependency:go-offline -B -Dmaven.wagon.http.retryHandler.count=3 || true

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B -Dmaven.wagon.http.retryHandler.count=3

# ---- Run stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
