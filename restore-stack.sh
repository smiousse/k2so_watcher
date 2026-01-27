#!/bin/bash
#
# Restore script for k2so-watcher Portainer stack
# Usage: ./restore-stack.sh <backup-file.tar.gz>
#

set -e

STACK_NAME="k2so-watcher"

# Check arguments
if [[ -z "$1" ]]; then
  echo "Usage: ./restore-stack.sh <backup-file.tar.gz>"
  echo ""
  echo "Example: ./restore-stack.sh backup-k2so-watcher-20240115-143022.tar.gz"
  exit 1
fi

BACKUP_FILE="$1"

if [[ ! -f "$BACKUP_FILE" ]]; then
  echo "Error: Backup file not found: $BACKUP_FILE"
  exit 1
fi

echo "============================================"
echo "  Restore: ${STACK_NAME} stack"
echo "============================================"
echo ""
echo "  Backup file: $BACKUP_FILE"
echo ""

# Check if stack is running
CONTAINERS=$(docker ps -q --filter "label=com.docker.compose.project=${STACK_NAME}" 2>/dev/null || true)
if [[ -n "$CONTAINERS" ]]; then
  echo "[!] Warning: Stack containers are running!"
  echo "    Please stop the stack in Portainer before restoring."
  echo ""
  read -p "    Stop containers now and continue? (y/N): " confirm
  if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "    Aborted."
    exit 1
  fi
  echo ""
  echo "    Stopping containers..."
  docker stop $CONTAINERS
fi

# Check for existing volumes
EXISTING_VOLUMES=$(docker volume ls -q | grep "^${STACK_NAME}" || true)
if [[ -n "$EXISTING_VOLUMES" ]]; then
  echo "[!] Warning: Existing volumes found:"
  echo "$EXISTING_VOLUMES" | sed 's/^/      - /'
  echo ""
  read -p "    Delete existing volumes and restore from backup? (y/N): " confirm
  if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "    Aborted."
    exit 1
  fi
  echo ""
  echo "    Removing existing volumes..."
  for volume in $EXISTING_VOLUMES; do
    docker volume rm "$volume"
    echo "      Removed: $volume"
  done
fi

# Extract backup
echo ""
echo "[1/3] Extracting backup archive..."
TEMP_DIR=$(mktemp -d)
tar xzf "$BACKUP_FILE" -C "$TEMP_DIR"

# Find the backup directory inside
BACKUP_DIR=$(find "$TEMP_DIR" -maxdepth 1 -type d -name "backup-*" | head -1)
if [[ -z "$BACKUP_DIR" ]]; then
  BACKUP_DIR="$TEMP_DIR"
fi

# Count volume archives
VOLUME_COUNT=$(find "$BACKUP_DIR" -name "*.tar.gz" | wc -l)
echo "      Found $VOLUME_COUNT volume(s) to restore"

# Restore each volume
echo ""
echo "[2/3] Restoring volumes..."
for archive in "$BACKUP_DIR"/*.tar.gz; do
  if [[ -f "$archive" ]]; then
    volume=$(basename "${archive%.tar.gz}")
    echo "      Restoring: $volume"

    # Create volume
    docker volume create "$volume" > /dev/null

    # Restore data
    docker run --rm \
      -v "${volume}:/target" \
      -v "${archive}:/backup.tar.gz:ro" \
      alpine sh -c "tar xzf /backup.tar.gz -C /target"
  fi
done

# Cleanup
echo ""
echo "[3/3] Cleaning up..."
rm -rf "$TEMP_DIR"

echo ""
echo "============================================"
echo "  Restore complete!"
echo "============================================"
echo ""
echo "  Restored volumes:"
docker volume ls | grep "${STACK_NAME}" | sed 's/^/    /'
echo ""
echo "  Next steps:"
echo "    1. Open Portainer"
echo "    2. Go to Stacks → Add stack"
echo "    3. Name: ${STACK_NAME}"
echo "    4. Paste your docker-compose.yml content"
echo "    5. Add environment variables if needed"
echo "    6. Deploy the stack"
echo ""
