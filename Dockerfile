# K2SO Watcher - Network Scanner Application
# Multi-stage build for optimized image size

# Stage 1: Build
FROM eclipse-temurin:17-jdk AS builder

# Install unzip for Maven wrapper
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

# Install network scanning tools and curl for health check
RUN apt-get update && apt-get install -y \
    arp-scan \
    nmap \
    net-tools \
    iproute2 \
    iputils-ping \
    dnsutils \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Create non-root user for security
RUN groupadd -r k2so && useradd -r -g k2so k2so

# Create data directory for H2 database
RUN mkdir -p /app/data && chown -R k2so:k2so /app

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown k2so:k2so app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV DB_PATH=/app/data/k2so_watcher
ENV SERVER_PORT=8080

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/login || exit 1

# Note: Running as root is required for arp-scan and nmap to work properly
# In production, consider using capabilities instead
USER root

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
