# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

K2SO Watcher is a Java Spring Boot network scanner application with a dark cyberpunk-themed web UI. It discovers and tracks devices on networks, identifies device types using heuristics and optional AI integration, and provides network security monitoring capabilities.

**Tech Stack**: Java 17, Spring Boot 3.2.1, Jetty, H2 Database, Spring Security with TOTP 2FA, Thymeleaf, Maven

## Build & Run Commands

```bash
# Build JAR
./mvnw clean package

# Run locally (requires network tools: arp-scan, nmap)
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=ClassName

# Run single test method
./mvnw test -Dtest=ClassName#methodName

# Docker build and run (recommended - includes all network tools)
docker compose up --build
```

## Architecture

```
Controllers → Services → Repositories → H2 Database
     ↓            ↓
 Thymeleaf    External
 Templates    (arp-scan, nmap, AI APIs)
```

### Key Services

- **NetworkScannerService** (`service/NetworkScannerService.java`): Core scanning logic using arp-scan, nmap, or ping. Runs scans in background threads, parses command output, creates/updates Device entities.

- **AIIdentificationService** (`service/AIIdentificationService.java`): Optional OpenAI/Claude API integration for device identification. Sends device metadata (MAC, vendor, hostname, ports) and receives AI-powered descriptions.

- **DeviceIdentificationService** (`service/DeviceIdentificationService.java`): Local heuristic-based device classification using vendor mappings and hostname patterns.

- **ScheduledScanService** (`service/ScheduledScanService.java`): Cron-based automated scans (configurable, default 2 AM daily).

### Data Models

- **Device**: Network device (MAC as unique key), tracks IP, hostname, vendor, device type, online status, open ports, OS detection, AI identification
- **NetworkScan**: Scan operation metadata (type, range, status, device counts, logs)
- **ScanResult**: Links Device to NetworkScan with per-scan status
- **User**: Authentication with BCrypt password, TOTP secret, role (ADMIN/USER), login lockout tracking

### Security

Spring Security with mandatory TOTP 2FA on first login. SecurityConfig enforces role-based access, CSRF protection, and max 1 concurrent session per user.

## Configuration

All settings in `src/main/resources/application.yml`. Key environment variables:

- `DB_PATH`: H2 database location (default: `./data/k2so_watcher`)
- `NETWORK_RANGE`: CIDR to scan (default: `192.168.1.0/24`)
- `SCANNER_TOOL`: `arp-scan`, `nmap`, or `ping` (default: `arp-scan`)
- `OPENAI_API_KEY` / `CLAUDE_API_KEY`: Optional AI integration

## Docker Requirements

Network scanning requires `network_mode: host` and capabilities `NET_RAW`, `NET_ADMIN` for arp-scan/nmap to function properly.
