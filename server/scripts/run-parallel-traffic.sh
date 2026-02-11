#!/usr/bin/env bash
set -euo pipefail

# Runs multiple "users" in parallel, each executing generate-traffic.sh with
# --random-scenario and --cycle against the order-scenarios.json file.
# A single Ctrl+C stops all users.
#
# Usage:
#   ./scripts/run-parallel-traffic.sh [N] [--verbose]
#   N                  Number of parallel users (default: 5).
#   --verbose, -v      Passed through to each worker.
#
# Example:
#   ./scripts/run-parallel-traffic.sh 10
#   ./scripts/run-parallel-traffic.sh 3 --verbose

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TRAFFIC_SCRIPT="${SCRIPT_DIR}/generate-traffic.sh"
SCENARIOS_FILE="${SCRIPT_DIR}/order-scenarios.json"
DEFAULT_USERS=5
USERS="${DEFAULT_USERS}"
EXTRA_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    -v|--verbose)
      EXTRA_ARGS+=(--verbose)
      shift
      ;;
    *)
      if [[ "$1" =~ ^[0-9]+$ ]]; then
        USERS="$1"
        shift
      else
        printf 'Unknown option or non-numeric user count: %s\n' "$1" >&2
        exit 1
      fi
      ;;
  esac
done

if [[ ! -f "${TRAFFIC_SCRIPT}" ]]; then
  printf 'Traffic script not found: %s\n' "${TRAFFIC_SCRIPT}" >&2
  exit 1
fi
if [[ ! -f "${SCENARIOS_FILE}" ]]; then
  printf 'Scenarios file not found: %s\n' "${SCENARIOS_FILE}" >&2
  exit 1
fi

printf '[%s] Starting %s parallel user(s). Press Ctrl+C to stop all.\n' "$(date '+%H:%M:%S')" "${USERS}"
PIDS=()
FIFOS=()
cleanup_fifos() {
  for f in "${FIFOS[@]}"; do rm -f "$f"; done
}
trap cleanup_fifos EXIT

for ((i=1; i<=USERS; i++)); do
  FIFO=$(mktemp -u "${TMPDIR:-/tmp}/parallel-traffic.XXXXXXXXXX")
  mkfifo "$FIFO"
  FIFOS+=("$FIFO")
  ( awk -v n="$i" '{printf "[%s]> %s\n", n, $0; fflush()}' < "$FIFO" ) &
  "${TRAFFIC_SCRIPT}" --scenarios="${SCENARIOS_FILE}" --random-scenario --cycle "${EXTRA_ARGS[@]}" > "$FIFO" 2>&1 &
  PIDS+=($!)
done

cleanup() {
  printf '\n[%s] Stopping all %s user(s) …\n' "$(date '+%H:%M:%S')" "${#PIDS[@]}"
  for pid in "${PIDS[@]}"; do
    kill -TERM "$pid" 2>/dev/null || true
  done
  wait "${PIDS[@]}" 2>/dev/null || true
  printf '[%s] All stopped.\n' "$(date '+%H:%M:%S')"
  exit 0
}

trap cleanup SIGINT SIGTERM
wait "${PIDS[@]}"
