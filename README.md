# K2SO Watcher - Network Scanner Application

A Java Spring Boot application that scans home networks to identify and track devices, featuring a dark futuristic UI inspired by cyberpunk aesthetics.

## Features

- Network device discovery using ARP scan, nmap, or ping sweep
- MAC vendor lookup for device identification
- AI-powered device identification (OpenAI/Claude integration)
- Two-factor authentication (TOTP) with QR code setup
- Scheduled nightly scans with new device alerts
- User management with role-based access (Admin/User)
- Dark neon cyberpunk UI theme

## Prerequisites

### For Docker Deployment (Recommended)
- Docker Engine 20.10+
- Docker Compose v2

### For Local Development
- Java 17+
- Maven 3.8+
- Network scanning tools (optional): `arp-scan`, `nmap`

## Quick Start with Docker

### 1. Build and Run

```bash
# Build the Docker image
docker compose build

# Start the container
docker compose up -d

# View logs
docker logs -f k2so-watcher
```

### 2. Access the Application

Open http://localhost:8080 in your browser.

**Default Credentials:**
- Username: `admin`
- Password: `admin`

On first login, you'll be prompted to set up two-factor authentication using an authenticator app (Google Authenticator, Authy, etc.).

### 3. Stop the Container

```bash
docker compose down
```

To also remove the data volume:
```bash
docker compose down -v
```

## Docker Commands Reference

| Command | Description |
|---------|-------------|
| `docker compose build` | Build the Docker image |
| `docker compose up -d` | Start container in background |
| `docker compose down` | Stop and remove container |
| `docker compose down -v` | Stop and remove container + data |
| `docker compose logs -f` | Follow container logs |
| `docker compose restart` | Restart the container |
| `docker ps` | List running containers |

## Local Development

### Build and Run

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/k2so-watcher-1.0.0.jar
```

### Development Mode

```bash
# Run with Maven (auto-reload not enabled)
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

## Configuration

### Environment Variables

Configure via `docker-compose.yml` or system environment:

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | HTTP server port |
| `ADMIN_USERNAME` | admin | Default admin username |
| `ADMIN_PASSWORD` | admin | Default admin password |
| `NETWORK_SCAN_RANGE` | 192.168.1.0/24 | Network range to scan |
| `SCANNER_TOOL` | arp-scan | Scan tool: `arp-scan`, `nmap`, or `ping` |
| `SCAN_TIMEOUT` | 120 | Scan timeout in seconds |
| `SCHEDULER_ENABLED` | true | Enable scheduled scans |
| `SCHEDULER_CRON` | 0 0 2 * * * | Scan schedule (default: 2 AM daily) |
| `AI_ENABLED` | false | Enable AI device identification |
| `AI_PROVIDER` | openai | AI provider: `openai` or `claude` |
| `AI_API_KEY` | | API key for AI provider |
| `AI_MODEL` | gpt-4 | AI model to use |

### Application Properties

Edit `src/main/resources/application.yml` for additional configuration options.

## Network Scanning Requirements

For network scanning to work properly, the application needs elevated privileges:

### Docker (Recommended)
The `docker-compose.yml` includes necessary capabilities:
- `NET_RAW` - Required for ARP scanning
- `NET_ADMIN` - Required for network interface access
- `network_mode: host` - Required to access local network

### Local/Bare Metal
```bash
# Run with sudo for network scanning capabilities
sudo java -jar target/k2so-watcher-1.0.0.jar

# Or grant capabilities to Java
sudo setcap cap_net_raw,cap_net_admin+eip $(which java)
```

## Project Structure

```
k2so_watcher/
├── src/main/java/com/k2so/watcher/
│   ├── config/          # Security, Web, Scheduler configs
│   ├── controller/      # Web and API controllers
│   ├── model/           # JPA entities
│   ├── repository/      # Data repositories
│   ├── service/         # Business logic
│   └── security/        # Authentication components
├── src/main/resources/
│   ├── templates/       # Thymeleaf HTML templates
│   ├── static/          # CSS, JavaScript
│   └── application.yml  # Configuration
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Data Persistence

- **Docker**: Data is stored in the `k2so-data` volume at `/app/data/`
- **Local**: Data is stored in `./data/` directory

The H2 database file (`k2so_watcher.mv.db`) contains all application data including users, devices, and scan history.

## Backup & Restore

The application includes built-in backup and restore functionality accessible from **Admin > Settings**.

### Creating a Backup

1. Navigate to **Admin > Settings**
2. Click **Create Backup**
3. A ZIP file will be downloaded containing the complete database

Backups include:
- All users and credentials
- Device inventory
- Scan history
- Application settings

### Restoring from Backup

1. Navigate to **Admin > Settings**
2. Click **Restore from Backup**
3. Select a previously downloaded backup ZIP file
4. Confirm the restore operation

**Warning**: Restoring a backup will replace ALL current data. You will be logged out after restore.

### Backup Storage

- Backups are stored in `./backups/` directory
- Maximum of 10 backups are kept (oldest are auto-deleted)
- Configure via `k2so.backup.directory` and `k2so.backup.max-files` properties

### Manual Backup (CLI)

```bash
# Copy the database file directly
cp ./data/k2so_watcher.mv.db ./my-backup.mv.db

# Or use Docker
docker cp k2so-watcher:/app/data/k2so_watcher.mv.db ./my-backup.mv.db
```

## Troubleshooting

### Container won't start
```bash
# Check logs for errors
docker logs k2so-watcher

# Verify port 8080 is available
lsof -i :8080
```

### Network scanning not working
- Ensure Docker is running with `network_mode: host`
- Verify `NET_RAW` and `NET_ADMIN` capabilities are set
- Check if `arp-scan` or `nmap` is installed in the container

### Reset admin password
Delete the database and restart:
```bash
# Docker
docker compose down -v
docker compose up -d

# Local
rm -rf ./data/k2so_watcher.*
java -jar target/k2so-watcher-1.0.0.jar
```

### TOTP not working
- Ensure your device time is synchronized
- Try regenerating the QR code by resetting the database

## Security Notes

- Change the default admin password after first login
- The application uses BCrypt for password hashing
- TOTP secrets are stored encrypted in the database
- CSRF protection is enabled for all forms
- Session timeout is configured for security

## License

This project is for personal/educational use.
