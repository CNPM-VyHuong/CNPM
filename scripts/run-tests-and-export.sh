#!/bin/bash
# Run all service tests and export metrics to Prometheus

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "📋 Running all service tests..."
echo "================================"

cd "$PROJECT_ROOT/DoAnCNPM_Backend"

services=(
  "user_service"
  "product_service"
  "order_service"
  "payment_service"
  "drone_service"
  "restaurant-service"
)

for service in "${services[@]}"; do
  if [ -d "$service" ]; then
    echo ""
    echo "🧪 Testing $service..."
    cd "$service"
    mvn clean test -q || echo "⚠️  Some tests failed in $service"
    cd ..
  fi
done

echo ""
echo "📊 Exporting metrics to Prometheus format..."
echo "=============================================="
cd "$PROJECT_ROOT"
python3 scripts/parse-junit-to-prometheus.py

echo ""
echo "✅ Done! Metrics available at:"
echo "   • Metrics file: monitoring/metrics/unit-tests.prom"
echo "   • Prometheus endpoint: http://localhost:9091/metrics"
echo "   • Grafana: http://localhost:3001"
