#!/usr/bin/env bash
set -euo pipefail

# Market Kafka / DLT helper
#
# Куда положить:
#   scripts/kafka-dlt-checks.sh
#
# Сделать исполняемым:
#   chmod +x scripts/kafka-dlt-checks.sh
#
# Если контейнер Kafka называется не "kafka":
#   KAFKA_CONTAINER=имя_контейнера ./scripts/kafka-dlt-checks.sh list-topics
#
# Если bootstrap-server внутри контейнера другой:
#   BOOTSTRAP=localhost:9092 ./scripts/kafka-dlt-checks.sh list-topics

KAFKA_CONTAINER="${KAFKA_CONTAINER:-kafka}"
BOOTSTRAP="${BOOTSTRAP:-localhost:9092}"

TOPIC_STOCK_REQUEST="market.order.stock-reservation-requested.v1"
TOPIC_CANCEL_REQUEST="market.order.cancellation-requested.v1"
TOPIC_STOCK_RESULT="market.product.stock-reservation-result.v1"
TOPIC_RELEASE_RESULT="market.product.stock-release-result.v1"

DLT_STOCK_REQUEST="${TOPIC_STOCK_REQUEST}.dlt"
DLT_CANCEL_REQUEST="${TOPIC_CANCEL_REQUEST}.dlt"
DLT_STOCK_RESULT="${TOPIC_STOCK_RESULT}.dlt"
DLT_RELEASE_RESULT="${TOPIC_RELEASE_RESULT}.dlt"

find_bin() {
  docker exec "$KAFKA_CONTAINER" sh -lc '
    for d in /opt/kafka/bin /opt/bitnami/kafka/bin /usr/bin; do
      if [ -x "$d/kafka-topics.sh" ]; then
        echo "$d"
        exit 0
      fi
    done
    echo "Kafka CLI scripts not found in container" >&2
    exit 1
  '
}

BIN="$(find_bin)"

usage() {
  cat <<EOF
Usage:
  $0 list-topics
  $0 watch-request-dlt
  $0 watch-cancel-dlt
  $0 watch-stock-result-dlt
  $0 watch-release-result-dlt
  $0 send-stock-request-no-event-id
  $0 send-cancel-request-no-event-id
  $0 send-stock-result-no-event-id
  $0 send-release-result-no-event-id
  $0 describe <topic>

Main test flow for non-retriable DLT:
  Terminal 1:
    $0 watch-request-dlt

  Terminal 2:
    $0 send-stock-request-no-event-id

Expected:
  product-service получает сообщение без eventId,
  выбрасывает NonRetryableKafkaEventException,
  сообщение сразу появляется в ${DLT_STOCK_REQUEST}.

EOF
}

list_topics() {
  docker exec "$KAFKA_CONTAINER" "$BIN/kafka-topics.sh" \
    --bootstrap-server "$BOOTSTRAP" \
    --list | sort
}

watch_topic() {
  local topic="$1"
  echo "Watching topic: $topic"
  echo "Press Ctrl+C to stop."
  docker exec -i "$KAFKA_CONTAINER" "$BIN/kafka-console-consumer.sh" \
    --bootstrap-server "$BOOTSTRAP" \
    --topic "$topic"
}

send_message() {
  local topic="$1"
  local payload="$2"

  echo "Sending message WITHOUT eventId header to topic: $topic"
  printf '%s\n' "$payload" | docker exec -i "$KAFKA_CONTAINER" "$BIN/kafka-console-producer.sh" \
    --bootstrap-server "$BOOTSTRAP" \
    --topic "$topic"
}

describe_topic() {
  local topic="$1"
  docker exec "$KAFKA_CONTAINER" "$BIN/kafka-topics.sh" \
    --bootstrap-server "$BOOTSTRAP" \
    --describe \
    --topic "$topic"
}

cmd="${1:-}"

case "$cmd" in
  list-topics)
    list_topics
    ;;

  watch-request-dlt)
    watch_topic "$DLT_STOCK_REQUEST"
    ;;

  watch-cancel-dlt)
    watch_topic "$DLT_CANCEL_REQUEST"
    ;;

  watch-stock-result-dlt)
    watch_topic "$DLT_STOCK_RESULT"
    ;;

  watch-release-result-dlt)
    watch_topic "$DLT_RELEASE_RESULT"
    ;;

  send-stock-request-no-event-id)
    send_message "$TOPIC_STOCK_REQUEST" '{"orderPublicId":"dlt-test-no-event-id","items":[]}'
    ;;

  send-cancel-request-no-event-id)
    send_message "$TOPIC_CANCEL_REQUEST" '{"orderPublicId":"dlt-test-cancel-no-event-id"}'
    ;;

  send-stock-result-no-event-id)
    send_message "$TOPIC_STOCK_RESULT" '{"orderPublicId":"dlt-test-result-no-event-id","success":true,"reason":null}'
    ;;

  send-release-result-no-event-id)
    send_message "$TOPIC_RELEASE_RESULT" '{"orderPublicId":"dlt-test-release-no-event-id","success":true,"reason":null}'
    ;;

  describe)
    if [ $# -lt 2 ]; then
      echo "Topic name is required"
      usage
      exit 1
    fi
    describe_topic "$2"
    ;;

  *)
    usage
    exit 1
    ;;
esac
