#!/bin/bash
# Script để thêm management.endpoints.web.exposure.include vào tất cả services

for dir in DoAnCNPM_Backend/*/; do
    service_name=$(basename "$dir")
    app_file="$dir/src/main/resources/application.yml"
    
    if [ -f "$app_file" ]; then
        # Check nếu chưa có management config
        if ! grep -q "management:" "$app_file"; then
            echo "Adding management config to $service_name..."
            cat >> "$app_file" << 'EOF'

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    health:
      show-details: always
EOF
        else
            echo "$service_name already has management config"
        fi
    else
        echo "application.yml not found for $service_name"
    fi
done

echo "✅ Done! All services now expose Prometheus metrics on /actuator/prometheus"
