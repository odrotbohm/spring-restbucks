#!/usr/bin/env bash
set -euo pipefail

# A tiny helper that drives one end-to-end order through the RESTBucks API.
# It follows the published affordances instead of hard-coding resource paths:
# 1) Fetch root and discover the placeOrder template (HAL-FORMS)
# 2) Pick the first available drink option and default location
# 3) POST a new order
# 4) Follow the payment link and pay with the built-in demo card
# 5) Poll until a receipt link appears, read it, then complete the order
#
# Usage:
#   ./scripts/generate-traffic.sh [--verbose|-v] [--force-error [INVALID_CARD|DOUBLE_PAY]] [--scenarios FILE] [--scenario NAME] [--base-url URL]
# Examples:
#   ./scripts/generate-traffic.sh
#   ./scripts/generate-traffic.sh --scenarios scripts/order-scenarios.json --verbose
#   ./scripts/generate-traffic.sh --scenarios scripts/order-scenarios.json --scenario java_chip_takeaway --force-error INVALID_CARD --base-url http://staging:8080
# Scenarios:
#   --scenarios FILE  Load scenarios from FILE (JSON array). Also accepts --scenarios=FILE. Without --scenario, execute all scenarios in order (force-error ignored).
#   --scenario NAME   Run only the named scenario (also accepts --scenario=NAME; force-error/base-url/verbose overrides apply).
# Error-path option:
#   --force-error     INVALID_CARD (default if no value) or DOUBLE_PAY.
#
# Requirements: curl, jq

ACCEPT_HAL_FORMS="application/prs.hal-forms+json"
ACCEPT_HAL_JSON="application/hal+json"
DEMO_CARD_NUMBER="1234123412341234"
BASE_URL="http://localhost:8080"
VERBOSE=false
FORCE_ERROR=""
FORCE_INVALID_CARD=false
FORCE_DOUBLE_PAY=false
SCENARIOS_PATH=""
SCENARIO_NAME=""
RUN_ALL_SCENARIOS=false
SCENARIOS_RAW=""
SCENARIO_COUNT=0
SCENARIO_JSON=""
SCENARIO_FORCE_ERROR=""
SCENARIO_LOCATION=""
SCENARIO_DRINK_NAMES=()

log_with_level() {
  local level="$1"; shift
  printf '[%s][%s] %s\n' "$(date '+%H:%M:%S')" "${level}" "$*"
}

log_info() {
  log_with_level "INFO" "$@"
}

log_debug() {
  if [[ "${VERBOSE}" == "true" ]]; then
    log_with_level "DEBUG" "$@"
  fi
}

log_debug_json() {
  if [[ "${VERBOSE}" != "true" ]]; then
    return
  fi

  local label="$1"
  local payload="$2"

  log_with_level "DEBUG" "${label}:"

  if [[ -z "${payload}" ]]; then
    printf '%s\n' ""
    return
  fi

  if command -v jq >/dev/null 2>&1; then
    printf '%s\n' "${payload}" | jq .
  else
    printf '%s\n' "${payload}"
  fi
}

need_tool() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Missing dependency: %s\n' "$1" >&2
    exit 1
  fi
}

strip_template() {
  # HAL templated links often look like ".../orders{?page,size,sort}"
  printf '%s' "$1" | sed 's/{.*$//'
}

load_scenario() {
  local scenarios_raw

  if [[ -z "${SCENARIOS_PATH}" ]]; then
    return
  fi

  if [[ ! -f "${SCENARIOS_PATH}" ]]; then
    printf 'Scenarios file not found: %s\n' "${SCENARIOS_PATH}" >&2
    exit 1
  fi

  scenarios_raw="$(cat "${SCENARIOS_PATH}")"

  local count
  count="$(echo "${scenarios_raw}" | jq 'length')"

  if [[ "${count}" -eq 0 ]]; then
    printf 'Scenarios file is empty: %s\n' "${SCENARIOS_PATH}" >&2
    exit 1
  fi

  if [[ -n "${SCENARIO_NAME}" ]]; then
    SCENARIO_JSON="$(echo "${scenarios_raw}" | jq --arg name "${SCENARIO_NAME}" 'map(select(.name==$name))[0]')"
    if [[ -z "${SCENARIO_JSON}" || "${SCENARIO_JSON}" == "null" ]]; then
      printf 'Scenario "%s" not found in scenarios file.\n' "${SCENARIO_NAME}" >&2
      exit 1
    fi
  else
    RUN_ALL_SCENARIOS=true
    SCENARIOS_RAW="${scenarios_raw}"
    SCENARIO_COUNT="${count}"
  fi

  if [[ "${RUN_ALL_SCENARIOS}" == "true" ]]; then
    log_info "Will run all ${SCENARIO_COUNT} scenarios from ${SCENARIOS_PATH} (force-error ignored)"
    return
  fi

  SCENARIO_FORCE_ERROR="$(echo "${SCENARIO_JSON}" | jq -r '.force_error // empty')"
  SCENARIO_LOCATION="$(echo "${SCENARIO_JSON}" | jq -r '.location // empty')"
  mapfile -t SCENARIO_DRINK_NAMES < <(echo "${SCENARIO_JSON}" | jq -r '.drinks[]?')

  if [[ -n "${SCENARIO_FORCE_ERROR}" ]]; then
    FORCE_ERROR="${FORCE_ERROR:-${SCENARIO_FORCE_ERROR}}"
  fi

  log_info "Using scenario '${SCENARIO_NAME}' from ${SCENARIOS_PATH}"
}

need_tool curl
need_tool jq

set_error_flags() {
  FORCE_INVALID_CARD=false
  FORCE_DOUBLE_PAY=false

  local upper
  upper="$(printf '%s' "${FORCE_ERROR}" | tr '[:lower:]' '[:upper:]')"

  case "${upper}" in
    INVALID_CARD)
      FORCE_INVALID_CARD=true
      ;;
    DOUBLE_PAY)
      FORCE_DOUBLE_PAY=true
      ;;
    "" )
      ;;
    *)
      printf 'Unknown force-error value: %s (expected INVALID_CARD or DOUBLE_PAY)\n' "${FORCE_ERROR}" >&2
      exit 1
      ;;
  esac
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -v|--verbose)
      VERBOSE=true
      shift
      ;;
    --force-error)
      if [[ $# -gt 1 && "$2" != "--"* ]]; then
        FORCE_ERROR="$2"
        shift 2
      else
        FORCE_ERROR="INVALID_CARD"
        shift
      fi
      ;;
    --scenarios)
      if [[ $# -gt 1 && "$2" != --* ]]; then
        SCENARIOS_PATH="$2"
        shift 2
      else
        printf '--scenarios requires a file path (e.g. --scenarios path/to/file.json)\n' >&2
        exit 1
      fi
      ;;
    --scenarios=*)
      SCENARIOS_PATH="${1#--scenarios=}"
      shift
      ;;
    --base-url)
      BASE_URL="$2"
      shift 2
      ;;
    --scenario)
      if [[ $# -gt 1 && "$2" != --* ]]; then
        SCENARIO_NAME="$2"
        shift 2
      else
        printf '--scenario requires a scenario name\n' >&2
        exit 1
      fi
      ;;
    --scenario=*)
      SCENARIO_NAME="${1#--scenario=}"
      shift
      ;;
    *)
      BASE_URL="$1"
      shift
      ;;
  esac
done

set_error_flags

log_info "Verbose mode: ${VERBOSE}"
log_info "Options: force-error=${FORCE_ERROR:-none}, scenarios=${SCENARIOS_PATH:-none}, scenario=${SCENARIO_NAME:-none}, base-url=${BASE_URL}"
log_info "Hitting root at ${BASE_URL}/ …"
ROOT_PAYLOAD="$(curl -fsSL -H "Accept: ${ACCEPT_HAL_FORMS}" "${BASE_URL}/")"
log_debug_json "Root payload" "${ROOT_PAYLOAD}"

ORDERS_URL="$(echo "${ROOT_PAYLOAD}" | jq -r '._links["restbucks:orders"].href | select(.!=null)')"
PLACE_TEMPLATE="$(echo "${ROOT_PAYLOAD}" | jq -c '._templates.placeOrder')"

if [[ -z "${ORDERS_URL}" || -z "${PLACE_TEMPLATE}" || "${PLACE_TEMPLATE}" == "null" ]]; then
  printf 'Could not discover orders link or placeOrder template. Is the server running?\n' >&2
  exit 1
fi

ORDERS_URL="$(strip_template "${ORDERS_URL}")"

DRINKS_OPTIONS_URL="$(echo "${PLACE_TEMPLATE}" | jq -r '.properties[] | select(.name=="drinks") | .options.link.href')"
LOCATION_OPTION="$(echo "${PLACE_TEMPLATE}" | jq -r '.properties[] | select(.name=="location") | .options.inline[0]')"

if [[ -z "${DRINKS_OPTIONS_URL}" || -z "${LOCATION_OPTION}" ]]; then
  printf 'Could not discover drink or location options from the template.\n' >&2
  exit 1
fi

DEFAULT_LOCATION_OPTION="${LOCATION_OPTION}"
DRINKS_OPTIONS_URL="$(strip_template "${DRINKS_OPTIONS_URL}")"

log_info "Fetching drinks options from ${DRINKS_OPTIONS_URL} …"
DRINKS_RESPONSE="$(curl -fsSL "${DRINKS_OPTIONS_URL}")"
DRINK_URI="$(echo "${DRINKS_RESPONSE}" | jq -r '._embedded["restbucks:drinks"][0].value')"
log_debug_json "Drinks response" "${DRINKS_RESPONSE}"

if [[ -z "${DRINK_URI}" || "${DRINK_URI}" == "null" ]]; then
  printf 'No drinks found to order. Seed data may be missing.\n' >&2
  exit 1
fi

load_scenario

if [[ "${RUN_ALL_SCENARIOS}" == "true" ]]; then
  SCENARIO_ITERATIONS="${SCENARIO_COUNT}"
else
  SCENARIO_ITERATIONS=1
fi

for ((SCENARIO_IDX=0; SCENARIO_IDX<SCENARIO_ITERATIONS; SCENARIO_IDX++)); do
  if [[ "${RUN_ALL_SCENARIOS}" == "true" ]]; then
    SCENARIO_JSON="$(echo "${SCENARIOS_RAW}" | jq --argjson i "${SCENARIO_IDX}" '.[$i]')"
  fi

  if [[ -n "${SCENARIO_JSON}" && "${SCENARIO_JSON}" != "null" ]]; then
    SCENARIO_FORCE_ERROR="$(echo "${SCENARIO_JSON}" | jq -r '.force_error // empty')"
    SCENARIO_LOCATION="$(echo "${SCENARIO_JSON}" | jq -r '.location // empty')"
    mapfile -t SCENARIO_DRINK_NAMES < <(echo "${SCENARIO_JSON}" | jq -r '.drinks[]? // empty' | grep -v '^$' || true)
  else
    SCENARIO_FORCE_ERROR=""
    SCENARIO_LOCATION=""
    SCENARIO_DRINK_NAMES=()
  fi

  if [[ "${RUN_ALL_SCENARIOS}" == "true" ]]; then
    FORCE_ERROR="${SCENARIO_FORCE_ERROR}"
  elif [[ -n "${SCENARIO_FORCE_ERROR}" ]]; then
    FORCE_ERROR="${FORCE_ERROR:-${SCENARIO_FORCE_ERROR}}"
  fi
  set_error_flags

  LOCATION_OPTION="${DEFAULT_LOCATION_OPTION}"
  if [[ -n "${SCENARIO_LOCATION}" ]]; then
    LOCATION_OPTION="${SCENARIO_LOCATION}"
  fi

  DRINK_URIS_JSON=""
  if [[ "${#SCENARIO_DRINK_NAMES[@]}" -gt 0 ]]; then
    declare -a DRINK_URIS=()
    for drink_name in "${SCENARIO_DRINK_NAMES[@]}"; do
      uri="$(echo "${DRINKS_RESPONSE}" | jq -r --arg name "${drink_name}" '._embedded["restbucks:drinks"][] | select(.prompt==$name) | .value')"
      if [[ -z "${uri}" || "${uri}" == "null" ]]; then
        printf 'Drink "%s" not found in options.\n' "${drink_name}" >&2
        exit 1
      fi
      DRINK_URIS+=("${uri}")
    done
    DRINK_URIS_JSON="$(printf '%s\n' "${DRINK_URIS[@]}" | jq -R . | jq -s .)"
  else
    DRINK_URIS_JSON="$(printf '%s\n' "${DRINK_URI}" | jq -R . | jq -s .)"
  fi

  if [[ -n "${SCENARIO_JSON}" && "${SCENARIO_JSON}" != "null" ]]; then
    SCENARIO_DISPLAY="$(echo "${SCENARIO_JSON}" | jq -r '.name // "unnamed"')"
  else
    SCENARIO_DISPLAY="default"
  fi
  log_info "Running scenario: ${SCENARIO_DISPLAY}"

  PAYLOAD="$(jq -n --argjson drinks "${DRINK_URIS_JSON}" --arg location "${LOCATION_OPTION}" '{drinks: $drinks, location: $location}')"
HEADERS_FILE="$(mktemp)"

log_info "Placing order at ${ORDERS_URL} using ${DRINK_URIS_JSON} and location ${LOCATION_OPTION} …"
ORDER_RESPONSE="$(curl -fsSL -D "${HEADERS_FILE}" \
  -H "Content-Type: application/json" \
  -H "Accept: ${ACCEPT_HAL_JSON}" \
  -X POST \
  -d "${PAYLOAD}" \
  "${ORDERS_URL}")"
log_debug_json "Order response body" "${ORDER_RESPONSE}"
log_debug "Order response headers:\n$(cat "${HEADERS_FILE}")"

ORDER_URL="$(grep -i '^Location:' "${HEADERS_FILE}" | awk '{print $2}' | tr -d '\r')"
rm -f "${HEADERS_FILE}"

if [[ -z "${ORDER_URL}" ]]; then
  ORDER_URL="$(echo "${ORDER_RESPONSE}" | jq -r '._links.self.href')"
fi

if [[ -z "${ORDER_URL}" || "${ORDER_URL}" == "null" ]]; then
  printf 'Order was created but no self link was discovered.\n' >&2
  exit 1
fi

log_info "Order created at ${ORDER_URL}"

ORDER_JSON="$(curl -fsSL -H "Accept: ${ACCEPT_HAL_JSON}" "${ORDER_URL}")"
log_debug_json "Order representation" "${ORDER_JSON}"
PAYMENT_URL="$(echo "${ORDER_JSON}" | jq -r '._links["restbucks:payment"].href | select(.!=null)')"

if [[ -z "${PAYMENT_URL}" ]]; then
  printf 'Payment link not present on order. Current state:\n%s\n' "${ORDER_JSON}" >&2
  exit 1
fi

log_info "Submitting payment to ${PAYMENT_URL} …"
PAYMENT_NUMBER="${DEMO_CARD_NUMBER}"
if [[ "${FORCE_INVALID_CARD}" == "true" ]]; then
  PAYMENT_NUMBER="9999999999999999"
fi

PAYMENT_BODY_FILE="$(mktemp)"
PAYMENT_STATUS="$(curl -sS -o "${PAYMENT_BODY_FILE}" -w '%{http_code}' \
  -X PUT \
  -H "Content-Type: application/json" \
  -H "Accept: ${ACCEPT_HAL_JSON}" \
  -d "{\"number\":\"${PAYMENT_NUMBER}\"}" \
  "${PAYMENT_URL}")"
PAYMENT_RESPONSE="$(cat "${PAYMENT_BODY_FILE}")"
rm -f "${PAYMENT_BODY_FILE}"

log_debug_json "Payment response" "${PAYMENT_RESPONSE}"

if [[ "${FORCE_INVALID_CARD}" == "true" ]]; then
  if [[ "${PAYMENT_STATUS}" -lt 400 ]]; then
    printf 'Expected payment failure with invalid card but got status %s\n' "${PAYMENT_STATUS}" >&2
    exit 1
  fi
  log_info "Invalid-card scenario triggered as expected (status ${PAYMENT_STATUS})."
  if [[ "${RUN_ALL_SCENARIOS}" == "true" ]]; then
    continue
  else
    exit 0
  fi
fi

if [[ "${PAYMENT_STATUS}" -lt 200 || "${PAYMENT_STATUS}" -ge 300 ]]; then
  printf 'Payment failed with status %s\n%s\n' "${PAYMENT_STATUS}" "${PAYMENT_RESPONSE}" >&2
  exit 1
fi

ORDER_URL="$(echo "${PAYMENT_RESPONSE}" | jq -r '._links.order.href // "'"${ORDER_URL}"'"')"

log_info "Waiting for receipt to be issued …"
RECEIPT_URL=""

for _ in {1..10}; do
  UPDATED_ORDER="$(curl -fsSL -H "Accept: ${ACCEPT_HAL_JSON}" "${ORDER_URL}")"
  RECEIPT_URL="$(echo "${UPDATED_ORDER}" | jq -r '._links["restbucks:receipt"].href // empty')"
  log_debug_json "Polled order" "${UPDATED_ORDER}"
  [[ -n "${RECEIPT_URL}" ]] && break
  sleep 2
done

if [[ -z "${RECEIPT_URL}" ]]; then
  printf 'Timed out waiting for receipt link.\n' >&2
  exit 1
fi

log_info "Receipt available at ${RECEIPT_URL}"
log_info "Reading receipt …"
curl -fsSL -H "Accept: ${ACCEPT_HAL_JSON}" "${RECEIPT_URL}" | jq .

if [[ "${FORCE_DOUBLE_PAY}" == "true" ]]; then
  log_info "Triggering second payment to force 'already paid' error …"
  SECOND_PAY_BODY_FILE="$(mktemp)"
  SECOND_PAY_HEADERS_FILE="$(mktemp)"
  SECOND_STATUS="$(curl -sS -o "${SECOND_PAY_BODY_FILE}" -D "${SECOND_PAY_HEADERS_FILE}" -w '%{http_code}' \
    -X PUT \
    -H "Content-Type: application/json" \
    -H "Accept: ${ACCEPT_HAL_JSON}" \
    -d "{\"number\":\"${DEMO_CARD_NUMBER}\"}" \
    "${PAYMENT_URL}")"
  SECOND_BODY="$(cat "${SECOND_PAY_BODY_FILE}")"
  SECOND_HEADERS="$(cat "${SECOND_PAY_HEADERS_FILE}")"
  rm -f "${SECOND_PAY_BODY_FILE}"
  rm -f "${SECOND_PAY_HEADERS_FILE}"

  log_debug "Second payment response headers (status ${SECOND_STATUS}):\n${SECOND_HEADERS}"
  if [[ -n "${SECOND_BODY}" ]]; then
    log_debug_json "Second payment response body" "${SECOND_BODY}"
  else
    log_debug "Second payment response body: <empty>"
  fi

  if [[ "${SECOND_STATUS}" -lt 400 ]]; then
    printf 'Expected second payment to fail, but got status %s\n' "${SECOND_STATUS}" >&2
    exit 1
  fi

  log_info "Second payment failed as expected (status ${SECOND_STATUS})."
fi

log_info "Taking receipt (completing order) …"
curl -fsSL -X DELETE "${RECEIPT_URL}" >/dev/null

log_info "Done. Order lifecycle completed successfully."
done

if [[ "${RUN_ALL_SCENARIOS}" == "true" ]]; then
  log_info "All ${SCENARIO_COUNT} scenarios completed."
fi

