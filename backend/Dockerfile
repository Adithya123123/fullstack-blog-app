# ==============================================================================
# Multi-Stage Dockerfile for Java 17 Full-Stack Blog Backend
# Optimized for Render Web Services & Container Deployments
# ==============================================================================

# Stage 1: Build the Application with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight JRE 17 Runtime Environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled fat JAR from builder stage
COPY --from=builder /app/target/blog-backend.jar app.jar

# Render assigns dynamic port via $PORT environment variable (defaults to 8080)
EXPOSE 8080
ENV PORT=8080

# Run backend
ENTRYPOINT ["java", "-jar", "app.jar"]
