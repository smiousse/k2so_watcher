# K2SO Watcher - Deployment Guide for Portainer

This guide explains how to deploy K2SO Watcher on a server with Portainer installed.

## Prerequisites

- Server with Docker installed
- Portainer installed and accessible
- Network access to the server on port 8080 (or your chosen port)

## Deployment Options

Choose one of the following deployment methods:

- **Option A**: Deploy using Portainer Stacks (recommended)
- **Option B**: Deploy using pre-built image from registry
- **Option C**: Build and deploy manually on the server

---

## Option A: Deploy Using Portainer Stacks (Recommended)

This method uses Portainer's Stack feature with Docker Compose.

### Step 1: Access Portainer

1. Open your Portainer web interface (typically `http://your-server:9000`)
2. Log in with your admin credentials
3. Select your Docker environment

### Step 2: Create a New Stack

1. In the left sidebar, click **Stacks**
2. Click **+ Add stack**
3. Enter a name: `k2so-watcher`

### Step 3: Configure the Stack

Choose one of the following methods:

#### Method 1: Web Editor (Paste Docker Compose)

1. Select **Web editor**
2. Paste the following Docker Compose configuration:

```yaml
services:
  k2so-watcher:
    image: ghcr.io/smiousse/k2so_watcher:latest  # Or build from source
    build:
      context: https://github.com/smiousse/k2so_watcher.git
      dockerfile: Dockerfile
    container_name: k2so-watcher
    network_mode: host
    ports:
      - 7777:8080
    volumes:
      - k2so-data:/app/data
      - k2so-backups:/app/backups
    environment:
      - SERVER_PORT=8080
      - ADMIN_USERNAME=admin
      - ADMIN_PASSWORD=Rjb5p9rw!
      - NETWORK_SCAN_RANGE=10.40.30.0/24
      - SCANNER_TOOL=nmap
      - SCHEDULER_ENABLED=true
      - SCHEDULER_CRON=0 0 2 * * *
      - AI_ENABLED=false
      - JAVA_OPTS=-Xmx512m -Xms256m -Duser.timezone=America/New_York
      - TZ=America/New_York
    cap_add:
      - NET_RAW
      - NET_ADMIN
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/login"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

volumes:
  k2so-data:
    driver: local
  k2so-backups:
    driver: local
```

#### Method 2: Git Repository

1. Select **Repository**
2. Enter your Git repository URL
3. Set the Compose path to `docker-compose.yml`

#### Method 3: Upload

1. Select **Upload**
2. Upload your `docker-compose.yml` file

### Step 4: Configure Environment Variables (Optional)

Scroll down to **Environment variables** and add any custom settings:

| Variable | Value | Description |
|----------|-------|-------------|
| `ADMIN_PASSWORD` | `your-secure-password` | Change default admin password |
| `NETWORK_SCAN_RANGE` | `192.168.1.0/24` | Your network range |
| `SERVER_PORT` | `8080` | Application port |

### Step 5: Deploy the Stack

1. Click **Deploy the stack**
2. Wait for the deployment to complete
3. Check the container status in the Stack details

### Step 6: Access the Application

1. Open `http://your-server:8080` in your browser
2. Log in with admin credentials
3. Complete the 2FA setup

---

## Option B: Deploy Using Pre-built Image

If you have pushed your image to a container registry (Docker Hub, GitHub Container Registry, etc.).

### Step 1: Push Image to Registry

On your local machine:

```bash
# Build the image
docker build -t your-registry/k2so-watcher:latest .

# Push to registry
docker login your-registry
docker push your-registry/k2so-watcher:latest
```

### Step 2: Create Container in Portainer

1. In Portainer, go to **Containers** > **+ Add container**
2. Configure the container:

| Setting | Value |
|---------|-------|
| **Name** | `k2so-watcher` |
| **Image** | `your-registry/k2so-watcher:latest` |
| **Network** | `host` |

3. Under **Advanced container settings**:

#### Volumes Tab
| Container Path | Host Path / Volume |
|---------------|-------------------|
| `/app/data` | `k2so-data` (volume) |
| `/app/backups` | `k2so-backups` (volume) |

#### Env Tab
Add environment variables:
```
SERVER_PORT=8080
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-secure-password
NETWORK_SCAN_RANGE=192.168.1.0/24
SCANNER_TOOL=arp-scan
SCHEDULER_ENABLED=true
```

#### Capabilities Tab
Add capabilities:
- `NET_RAW`
- `NET_ADMIN`

#### Restart Policy Tab
Select: **Unless stopped**

4. Click **Deploy the container**

---

## Option C: Build and Deploy on Server

Build the image directly on your server.

### Step 1: Transfer Files to Server

```bash
# Option 1: Clone from Git
ssh user@your-server
git clone https://github.com/your-username/k2so-watcher.git
cd k2so-watcher

# Option 2: Copy files via SCP
scp -r ./k2so_watcher user@your-server:/opt/k2so-watcher
```

### Step 2: Build Image via Portainer

1. In Portainer, go to **Images** > **Build a new image**
2. Enter image name: `k2so-watcher:latest`
3. Select **Upload** and upload the project files, OR
4. Select **URL** and enter the path to your Dockerfile

### Step 3: Deploy Using Stack

Follow **Option A** steps, but use the local image:

```yaml
services:
  k2so-watcher:
    image: k2so-watcher:latest
    # ... rest of configuration
```

---

## Post-Deployment Configuration

### 1. Change Default Password

After first login:
1. Go to **Admin** > **Users**
2. Edit the admin user
3. Change the password

### 2. Configure Network Scan Range

1. Go to **Admin** > **Settings**
2. Update `network.scan.range` to match your network (e.g., `192.168.1.0/24`)

### 3. Set Up Scheduled Scans

The default schedule runs at 2 AM daily. To change:
1. Go to **Admin** > **Settings**
2. Update `scheduler.cron` with your preferred schedule

Cron format: `seconds minutes hours day month weekday`
- `0 0 2 * * *` - Daily at 2:00 AM
- `0 0 */6 * * *` - Every 6 hours
- `0 30 8 * * MON-FRI` - Weekdays at 8:30 AM

### 4. Configure AI Integration (Optional)

To enable AI-powered device identification:
1. Go to **Admin** > **Settings**
2. Set `ai.enabled` to `true`
3. Set `ai.provider` to `openai` or `claude`
4. Set `ai.api.key` to your API key

---

## Updating the Application

### Using Portainer Stacks

1. Go to **Stacks** > **k2so-watcher**
2. Click **Editor**
3. Update the image tag if needed
4. Click **Update the stack**
5. Check **Re-pull image** and **Redeploy**

### Using Container

1. Go to **Containers**
2. Stop the `k2so-watcher` container
3. Go to **Images** and pull the new image
4. Recreate the container with the same settings

### Command Line

```bash
# Pull latest image
docker pull your-registry/k2so-watcher:latest

# Recreate container
docker compose down
docker compose up -d
```

---

## Backup Before Updates

Always create a backup before updating:

1. Access the application at `http://your-server:8080`
2. Go to **Admin** > **Settings**
3. Click **Create Backup**
4. Save the downloaded ZIP file

Or via command line:
```bash
# Copy database from container
docker cp k2so-watcher:/app/data/k2so_watcher.mv.db ./backup-$(date +%Y%m%d).mv.db

# Or copy the entire data volume
docker run --rm -v k2so-data:/data -v $(pwd):/backup alpine tar czf /backup/k2so-backup.tar.gz /data
```

---

## Troubleshooting

### Container won't start

Check logs in Portainer:
1. Go to **Containers** > **k2so-watcher**
2. Click **Logs**

Or via command line:
```bash
docker logs k2so-watcher
```

### Network scanning not working

1. Verify capabilities are set:
   - Go to **Containers** > **k2so-watcher** > **Inspect**
   - Check `CapAdd` includes `NET_RAW` and `NET_ADMIN`

2. Verify network mode:
   - Must be `host` for network scanning to work

3. Test manually:
   ```bash
   docker exec k2so-watcher arp-scan --localnet
   ```

### Port already in use

Change the `SERVER_PORT` environment variable to a different port.

### Cannot access from browser

1. Check firewall rules on the server
2. Verify the container is running
3. Check the correct port is being used

```bash
# Check if port is listening
ss -tlnp | grep 8080

# Check firewall (Ubuntu/Debian)
sudo ufw status

# Allow port if needed
sudo ufw allow 8080/tcp
```

---

## Security Recommendations

1. **Change default credentials** immediately after deployment
2. **Use HTTPS** - Put a reverse proxy (Nginx, Traefik) in front with SSL
3. **Restrict access** - Use firewall rules to limit access to trusted IPs
4. **Regular backups** - Schedule regular backups of the database
5. **Keep updated** - Regularly update the application and base images

### Example: Nginx Reverse Proxy with SSL

```nginx
server {
    listen 443 ssl;
    server_name k2so.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/k2so.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/k2so.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## Quick Reference

| Task | Location in Portainer |
|------|----------------------|
| View logs | Containers > k2so-watcher > Logs |
| Restart | Containers > k2so-watcher > Restart |
| Stop | Containers > k2so-watcher > Stop |
| Shell access | Containers > k2so-watcher > Console |
| Update stack | Stacks > k2so-watcher > Editor |
| View volumes | Volumes > k2so-data |

| URL | Description |
|-----|-------------|
| `http://server:8080` | Application |
| `http://server:8080/login` | Login page |
| `http://server:8080/admin/settings` | Settings & Backup |
| `http://server:9000` | Portainer (default) |
