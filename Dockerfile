# Use a verified available Maven image
FROM maven:3.9.6-eclipse-temurin-21 AS build

LABEL authors="murde"

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage - Usamos una imagen más ligera para Render
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks (alpine uses apk instead of apt)
RUN apk add --no-cache curl

# Create app directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory and set permissions (BEFORE switching user)
RUN mkdir -p /app/logs && chown appuser:appuser /app/logs

# Change ownership to appuser
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port (Render usa el puerto 10000 por defecto)
EXPOSE 10000

# Health check adaptado para Render
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:10000/actuator/health || exit 1

# Run the application con variables de entorno para Render
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]