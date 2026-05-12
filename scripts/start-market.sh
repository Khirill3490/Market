##!/usr/bin/env bash
#
#set -euo pipefail
#
#PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
#cd "$PROJECT_ROOT"
#
#LOG_DIR="$PROJECT_ROOT/logs/market-start"
#PID_DIR="$LOG_DIR/pids"
#
#mkdir -p "$LOG_DIR"
#mkdir -p "$PID_DIR"
#
#echo "======================================"
#echo "Starting Market backend stack"
#echo "Project root: $PROJECT_ROOT"
#echo "Logs: $LOG_DIR"
#echo "======================================"
#
#wait_for_http() {
#  local name="$1"
#  local url="$2"
#  local timeout_seconds="${3:-120}"
#
#  echo "Waiting for $name: $url"
#
#  local start_time
#  start_time=$(date +%s)
#
#  until curl -fsS "$url" >/dev/null 2>&1; do
#    local now
#    now=$(date +%s)
#
#    if (( now - start_time > timeout_seconds )); then
#      echo "ERROR: $name is not ready after ${timeout_seconds}s"
#      echo "URL: $url"
#      exit 1
#    fi
#
#    sleep 2
#  done
#
#  echo "$name is ready"
#}
#
#wait_for_postgres() {
#  local timeout_seconds="${1:-120}"
#
#  echo "Waiting for PostgreSQL..."
#
#  local start_time
#  start_time=$(date +%s)
#
#  until docker exec postgres_db pg_isready -U postgres -d app_db >/dev/null 2>&1; do
#    local now
#    now=$(date +%s)
#
#    if (( now - start_time > timeout_seconds )); then
#      echo "ERROR: PostgreSQL is not ready after ${timeout_seconds}s"
#      exit 1
#    fi
#
#    sleep 2
#  done
#
#  echo "PostgreSQL is ready"
#}
#
#wait_for_eureka_app() {
#  local app_name="$1"
#  local timeout_seconds="${2:-120}"
#
#  echo "Waiting for Eureka app registration: $app_name"
#
#  local start_time
#  start_time=$(date +%s)
#
#  until curl -fsS -H "Accept: application/json" "http://localhost:8761/eureka/apps/$app_name" >/dev/null 2>&1; do
#    local now
#    now=$(date +%s)
#
#    if (( now - start_time > timeout_seconds )); then
#      echo "ERROR: $app_name was not registered in Eureka after ${timeout_seconds}s"
#      echo "Check logs in: $LOG_DIR"
#      exit 1
#    fi
#
#    sleep 2
#  done
#
#  echo "$app_name is registered in Eureka"
#}
#
#start_service() {
#  local module="$1"
#  local service_name="$2"
#
#  local pid_file="$PID_DIR/$service_name.pid"
#  local log_file="$LOG_DIR/$service_name.log"
#
#  if [[ -f "$pid_file" ]]; then
#    local old_pid
#    old_pid="$(cat "$pid_file")"
#
#    if kill -0 "$old_pid" >/dev/null 2>&1; then
#      echo "$service_name is already running. PID: $old_pid"
#      return
#    fi
#  fi
#
#  echo "Starting $service_name..."
#  echo "Log file: $log_file"
#
#  nohup ./gradlew ":$module:bootRun" > "$log_file" 2>&1 &
#  local pid="$!"
#
#  echo "$pid" > "$pid_file"
#  echo "$service_name started. PID: $pid"
#}
#
#echo
#echo "1) Starting Docker infrastructure..."
#docker compose -f docker/docker-compose.yaml up -d
#
#wait_for_postgres 120
#wait_for_http "Keycloak market realm" "http://localhost:8088/realms/market/.well-known/openid-configuration" 180
#
#echo
#echo "2) Starting Eureka..."
#start_service "eureka-server" "eureka-server"
#wait_for_http "Eureka" "http://localhost:8761" 120
#
#echo
#echo "3) Starting auth-module..."
#start_service "auth-module" "auth-module"
#wait_for_eureka_app "AUTH-MODULE" 180
#
#echo
#echo "4) Starting product-service and user-service..."
#start_service "product-service" "product-service"
#start_service "user-service" "user-service"
#
#wait_for_eureka_app "PRODUCT-SERVICE" 180
#wait_for_eureka_app "USER-SERVICE" 180
#
#echo
#echo "5) Starting api-gateway..."
#start_service "api-gateway" "api-gateway"
#wait_for_http "API Gateway" "http://localhost:8080" 120
#wait_for_eureka_app "API-GATEWAY" 120 || true
#
#echo
#echo "======================================"
#echo "Market backend stack started"
#echo "======================================"
#echo "Eureka:    http://localhost:8761"
#echo "Gateway:   http://localhost:8080"
#echo "Keycloak:  http://localhost:8088"
#echo
#echo "Logs:"
#echo "  $LOG_DIR/eureka-server.log"
#echo "  $LOG_DIR/auth-module.log"
#echo "  $LOG_DIR/product-service.log"
#echo "  $LOG_DIR/user-service.log"
#echo "  $LOG_DIR/api-gateway.log"
#echo "======================================"