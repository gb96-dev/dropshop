#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="${1:-}"
MIN_DISK_MB="${2:-1024}"

if [ -z "${DEPLOY_DIR}" ]; then
  echo "[preflight] usage: ec2-preflight.sh <deploy_dir> [min_disk_mb]"
  exit 1
fi

echo "[preflight] checking docker binary"
command -v docker >/dev/null

echo "[preflight] checking docker compose plugin"
docker compose version >/dev/null

echo "[preflight] checking deploy directory permissions: ${DEPLOY_DIR}"
mkdir -p "${DEPLOY_DIR}"
if [ ! -w "${DEPLOY_DIR}" ]; then
  echo "[preflight] deploy directory is not writable: ${DEPLOY_DIR}"
  exit 1
fi

echo "[preflight] checking available disk space"
AVAILABLE_MB="$(df -Pm "${DEPLOY_DIR}" | awk 'NR==2 {print $4}')"
if [ -z "${AVAILABLE_MB}" ]; then
  echo "[preflight] failed to detect available disk space"
  exit 1
fi

if [ "${AVAILABLE_MB}" -lt "${MIN_DISK_MB}" ]; then
  echo "[preflight] insufficient disk space: ${AVAILABLE_MB}MB < ${MIN_DISK_MB}MB"
  exit 1
fi

echo "[preflight] success: docker/compose/permission/disk checks passed"

