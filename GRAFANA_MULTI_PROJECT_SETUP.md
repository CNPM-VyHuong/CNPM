# Hướng Dẫn Setup Grafana Hiển Thị Nhiều Project

## 📊 Overview
Hệ thống metrics được thiết kế để hỗ trợ nhiều project/services với service labels trong Prometheus.

## 🔧 1. Metrics Server - Phục Vụ Metrics

### File: `monitoring/metrics-server.py`
Server này expose metrics cho Prometheus scrape:
- **Port**: 9091
- **Endpoint**: `http://localhost:9091/metrics`
- **Format**: Prometheus text format
- **Frequency**: Tự động load từ test_metrics.txt

```bash
python monitoring/metrics-server.py
```

## 📈 2. Prometheus Configuration

### File: `monitoring/prometheus.yml`
Cấu hình Prometheus scrape metrics từ metrics-server:

```yaml
scrape_configs:
  - job_name: 'test-metrics'
    static_configs:
      - targets: ['localhost:9091']
    scrape_interval: 30s
    scrape_timeout: 10s
```

**Query Examples:**
```promql
# Tất cả tests
test_count_total

# Tests theo service
test_count_by_service{service="user_service"}

# Pass rate theo service
test_pass_rate_by_service

# Failed tests theo service  
test_fail_count_by_service
```

## 🎨 3. Grafana Dashboard Configuration

### A. Tạo Dashboard Mới

**Steps:**
1. Vào Grafana: `http://localhost:3000`
2. Click `+ Create` → `Dashboard`
3. Click `Add a new panel`

### B. Panel Types & Queries

#### **1. Service Count Table**
```promql
test_count_by_service{}
```
- **Format**: Table
- **Columns**: Service, Count
- **Transformations**: 
  - Organize: Rename `__name__` → "Metric"

#### **2. Service Pass Rate Gauge**
```promql
test_pass_rate_by_service{} * 100
```
- **Type**: Stat
- **Unit**: percent
- **Thresholds**: 
  - Green: > 95%
  - Yellow: > 80%
  - Red: < 80%

#### **3. Service Comparison Bar Chart**
```promql
test_pass_count_by_service{}
test_fail_count_by_service{}
```
- **Type**: Bar Chart
- **Legend**: Pass/Fail per service

#### **4. Test Execution Timeline**
```promql
test_execution_time_by_service{}
```
- **Type**: Time Series
- **Unit**: seconds
- **Stack**: Normal

#### **5. Pass Rate Trend (Multi-service)**
```promql
test_pass_rate_by_service{}
```
- **Type**: Time Series  
- **Legend**: `{{service}}`
- **Interpolation**: Smooth

### C. Dashboard Variables (Optional)

**Variable: `service`**
- **Type**: Query
- **Query**: `label_values(test_count_by_service, service)`
- **Multi-select**: Yes

**Usage in Panel:**
```promql
test_pass_rate_by_service{service=~"$service"}
```

## 🚀 4. Running Everything

### **Option 1: Docker Compose (Recommended)**
```bash
cd d:\cnpm\CNPM-3
docker-compose up -d
```

Services:
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001` 
- Metrics Server: `http://localhost:9091/metrics`

### **Option 2: Manual Setup**

**Terminal 1 - Metrics Server:**
```bash
cd d:\cnpm\CNPM-3
python monitoring/metrics-server.py
```

**Terminal 2 - Prometheus:**
```bash
cd d:\cnpm\CNPM-3
prometheus --config.file=monitoring/prometheus.yml
```

**Terminal 3 - Grafana:**
```bash
docker run -d -p 3001:3000 grafana/grafana:latest
```

## 📊 5. Existing Dashboard Panels

Dashboard: `services-dashboard` (UID: `fastfood-services`)

### Current Panels:
1. ✅ **Services Up** - Count of running services
2. ✅ **p95 Response** - Response time metric
3. ✅ **Error Rate** - 5xx error rate
4. ✅ **Request Rate** - HTTP requests/sec
5. ✅ **Memory Usage** - JVM heap memory
6. ✅ **CPU Usage** - System CPU
7. ✅ **Active Threads** - JVM threads
8. ✅ **Test Results by Service** - Pass/Fail/Total
9. ✅ **Success Rate by Service %** - Pass rate per service
10. ✅ **Test Rate Trends** - Test counts over time
11. ✅ **Test Execution Time** - Execution time per service

### Panel Details:

**Test Results by Service:**
```promql
test_pass_count_by_service{}    # ✅ Passed
test_fail_count_by_service{}    # ❌ Failed  
test_count_by_service{}         # 📊 Total
```

**Success Rate by Service:**
```promql
test_pass_rate_by_service{} * 100  # Returns 0-100
```

**Test Execution Time:**
```promql
test_execution_time_by_service{}   # Returns seconds
```

## 📝 6. Adding New Project/Service

### Step 1: Update Script
Edit `scripts/test_metrics_parser.py` - Add service to list:
```python
self.services = [
    'user_service',
    'product_service',
    'drone_service',
    'order_service',
    'payment_service',
    'restaurant-service',
    'new_service_name'  # ← Add here
]
```

### Step 2: Run Tests
```bash
cd DoAnCNPM_Backend/new_service_name
mvn test
```

### Step 3: Parse Metrics
```bash
python scripts/test_metrics_parser.py DoAnCNPM_Backend
```

### Step 4: Restart Metrics Server
- Metrics automatically reload from test_metrics.txt
- Grafana queries automatically show new service in labels

## 🔍 7. Troubleshooting

### Q: Metrics not showing in Grafana?
**A:**
```bash
# Check metrics endpoint
curl http://localhost:9091/metrics

# Check Prometheus scrape
http://localhost:9090/targets
```

### Q: Service not appearing?
**A:**
1. Check test reports exist: `DoAnCNPM_Backend/service_name/target/surefire-reports/TEST-*.xml`
2. Re-run parser: `python scripts/test_metrics_parser.py`
3. Verify metrics file: `cat monitoring/metrics/test_metrics.txt`

### Q: How to filter by service in Grafana?
**A:** 
Use label matchers in Prometheus queries:
```promql
test_pass_rate_by_service{service="user_service"}
test_pass_rate_by_service{service=~"user|product"}  # Regex
test_pass_rate_by_service{service!="drone"}         # Exclude
```

## 📚 8. Metrics Reference

| Metric | Type | Labels | Example Query |
|--------|------|--------|---|
| `test_count_total` | Counter | - | `test_count_total` |
| `test_pass_count` | Counter | - | `test_pass_count` |
| `test_fail_count` | Counter | - | `test_fail_count` |
| `test_pass_rate_percent` | Gauge | - | `test_pass_rate_percent` |
| `test_execution_time_seconds` | Gauge | - | `test_execution_time_seconds` |
| `test_count_by_service` | Gauge | `service` | `test_count_by_service{service="user_service"}` |
| `test_pass_count_by_service` | Gauge | `service` | `test_pass_count_by_service{}` |
| `test_fail_count_by_service` | Gauge | `service` | `test_fail_count_by_service{}` |
| `test_pass_rate_by_service` | Gauge | `service` | `test_pass_rate_by_service{}` |
| `test_execution_time_by_service` | Gauge | `service` | `test_execution_time_by_service{}` |

## 🎯 9. Quick Commands

```bash
# Parse metrics from all services
python scripts/test_metrics_parser.py D:\cnpm\CNPM-3\DoAnCNPM_Backend

# View parsed metrics
cat monitoring/metrics/test_metrics.txt

# View JSON metrics
cat monitoring/metrics/test_metrics.json

# Restart Docker stack
docker-compose restart

# View logs
docker-compose logs -f prometheus
docker-compose logs -f grafana
```

---

**Version**: 1.0  
**Last Updated**: 2025-11-26  
**Support**: Multi-project, Multi-service metrics dashboard
