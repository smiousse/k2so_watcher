# K2SO Watcher Stack Backup & Restore Guide

This guide covers how to backup and restore the k2so-watcher Portainer stack, including all volume data.

## Prerequisites

- Docker installed on both source and target servers
- Access to Portainer UI to export/import the docker-compose.yml
- Sufficient disk space for backup archives

## Files

| File | Description |
|------|-------------|
| `backup-stack.sh` | Creates a full backup of all stack volumes |
| `restore-stack.sh` | Restores volumes from a backup archive |

---

## Backup Process

### Step 1: Export docker-compose.yml from Portainer

1. Open Portainer UI
2. Go to **Stacks** → **k2so-watcher**
3. Click **Editor** tab
4. Copy the entire YAML content
5. Save to a file: `docker-compose.yml`
6. Go to **Environment variables** tab
7. Note all variables (save to `.env` file if needed)

### Step 2: Run Backup Script

```bash
# Full backup (stops containers for data consistency, then restarts)
./backup-stack.sh

# Backup without stopping containers (faster but less safe for databases)
./backup-stack.sh --no-stop
```

### What the script does

1. Creates a timestamped backup directory
2. Finds all volumes matching `k2so-watcher*`
3. Stops stack containers (unless `--no-stop` flag used)
4. Backs up each volume to a `.tar.gz` archive
5. Restarts containers
6. Creates final archive: `backup-k2so-watcher-YYYYMMDD-HHMMSS.tar.gz`

### Backup Output

```
============================================
  Backup complete!
============================================

  File: backup-k2so-watcher-20240115-143022.tar.gz
  Size: 125M

  Remember to also save your docker-compose.yml
  from Portainer: Stacks → k2so-watcher → Editor
```

---

## Restore Process

### Step 1: Transfer Files to New Server

Copy to the new server:
- `backup-k2so-watcher-*.tar.gz` (backup archive)
- `docker-compose.yml` (from Portainer export)
- `restore-stack.sh` (restore script)
- `.env` (if you have environment variables)

### Step 2: Run Restore Script

```bash
./restore-stack.sh backup-k2so-watcher-20240115-143022.tar.gz
```

### What the script does

1. Checks if stack containers are running (prompts to stop)
2. Checks for existing volumes (prompts to delete)
3. Extracts the backup archive
4. Creates each volume and restores data
5. Displays next steps

### Step 3: Create Stack in Portainer

1. Open Portainer UI on the new server
2. Go to **Stacks** → **Add stack**
3. Name: `k2so-watcher`
4. Select **Web editor**
5. Paste the contents of your `docker-compose.yml`
6. Add environment variables if needed
7. Click **Deploy the stack**

---

## Full Migration Workflow

### On Source Server

```bash
# 1. Run backup
./backup-stack.sh

# 2. Copy files to new server
scp backup-k2so-watcher-*.tar.gz user@new-server:/path/to/
scp docker-compose.yml user@new-server:/path/to/
scp restore-stack.sh user@new-server:/path/to/
```

### On Target Server

```bash
# 1. Make script executable
chmod +x restore-stack.sh

# 2. Restore volumes
./restore-stack.sh backup-k2so-watcher-*.tar.gz

# 3. Create stack in Portainer UI with docker-compose.yml
```

---

## Troubleshooting

### No volumes found

If the backup script reports no volumes:

```bash
# List all Docker volumes
docker volume ls

# Check if stack uses a different naming convention
docker volume ls | grep -i k2so
```

### Permission denied

```bash
# Make scripts executable
chmod +x backup-stack.sh restore-stack.sh
```

### Containers won't stop

Stop via Portainer UI instead:
1. Go to **Stacks** → **k2so-watcher**
2. Click **Stop this stack**

### Restore fails with existing volumes

Either:
- Let the script delete them (answer 'y' when prompted)
- Manually remove: `docker volume rm <volume-name>`

---

## Important Notes

- **Database consistency**: Use the default mode (with container stop) for the H2 database to ensure data consistency
- **Backup storage**: Store backups on external storage or different server
- **Test restores**: Periodically test the restore process on a test environment
- **Compose file**: Always save the docker-compose.yml separately - it's stored in Portainer's database, not in the volumes
