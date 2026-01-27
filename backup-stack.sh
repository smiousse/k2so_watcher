#!/bin/bash
#
# Backup script for k2so-watcher Portainer stack
# Usage: ./backup-stack.sh [--no-stop]
#

set -e

STACK_NAME="k2so-watcher"
BACKUP_DIR="backup-${STACK_NAME}-$(date +%Y%m%d-%H%M%S)"
NO_STOP=false

# Parse arguments
if [[ "$1" == "--no-stop" ]]; then
  NO_STOP=true
fi

echo "============================================"
echo "  Backup: ${STACK_NAME} stack"
echo "============================================"
echo ""

# Create backup directory
mkdir -p "$BACKUP_DIR"
echo "[1/5] Created backup directory: $BACKUP_DIR"

# Find volumes for this stack
VOLUMES=$(docker volume ls -q | grep "^${STACK_NAME}" || true)

if [[ -z "$VOLUMES" ]]; then
  echo "[!] No volumes found for stack '${STACK_NAME}'"
  echo "    Listing all volumes:"
  docker volume ls
  exit 1
fi

echo "[2/5] Found volumes:"
echo "$VOLUMES" | sed 's/^/      - /'
echo ""

# Stop containers for consistent backup
if [[ "$NO_STOP" == false ]]; then
  echo "[3/5] Stopping stack containers..."
  CONTAINERS=$(docker ps -q --filter "label=com.docker.compose.project=${STACK_NAME}" 2>/dev/null || true)
  if [[ -n "$CONTAINERS" ]]; then
    docker stop $CONTAINERS
    echo "      Containers stopped"
  else
    echo "      No running containers found (may already be stopped)"
  fi
else
  echo "[3/5] Skipping container stop (--no-stop flag)"
fi

# Backup each volume
echo "[4/5] Backing up volumes..."
for volume in $VOLUMES; do
  echo "      Backing up: $volume"
  docker run --rm \
    -v "${volume}:/source:ro" \
    -v "$(pwd)/${BACKUP_DIR}:/backup" \
    alpine tar czf "/backup/${volume}.tar.gz" -C /source .
done

# Restart containers if we stopped them
if [[ "$NO_STOP" == false ]]; then
  echo "      Restarting containers..."
  if [[ -n "$CONTAINERS" ]]; then
    docker start $CONTAINERS
    echo "      Containers restarted"
  fi
fi

# Create final archive
echo "[5/5] Creating final archive..."
tar czf "${BACKUP_DIR}.tar.gz" "$BACKUP_DIR"
rm -rf "$BACKUP_DIR"

echo ""
echo "============================================"
echo "  Backup complete!"
echo "============================================"
echo ""
echo "  File: ${BACKUP_DIR}.tar.gz"
echo "  Size: $(ls -lh "${BACKUP_DIR}.tar.gz" | awk '{print $5}')"
echo ""
echo "  Remember to also save your docker-compose.yml"
echo "  from Portainer: Stacks → k2so-watcher → Editor"
echo ""
