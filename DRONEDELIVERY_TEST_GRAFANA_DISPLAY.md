# Hiển thị Test Metrics của DroneDelivery-main trong Grafana

## 📊 Test Summary

Các test của DroneDelivery-main đã được chạy thành công:

### Unit Tests Results:
- **Passed**: 25 tests ✅
- **Failed**: 1 test ❌
- **Total**: 26 tests
- **Coverage**: 
  - Statements: 1.77%
  - Branches: 0.75%
  - Functions: 4.42%
  - Lines: 1.73%

### Test Files:
1. `__tests__/unit/drone.model.test.js` - ✅ PASS (10 tests)
2. `__tests__/unit/user.model.test.js` - ✅ PASS (10 tests)
3. `__tests__/unit/order.model.test.js` - ❌ FAIL (1 failed, 5 passed)

---

## 🔧 Cấu hình Prometheus

### File cấu hình: `monitoring/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'dronedelivery-tests'
    static_configs:
      - targets: ['metrics-server:8000']
    metrics_path: '/metrics/dronedelivery_test_metrics.txt'
```

---

## 📈 Hiển thị trong Grafana

### 1. Truy cập Grafana
- **URL**: `http://localhost:3000`
- **Username**: `admin`
- **Password**: `admin`

### 2. Thêm Data Source (Prometheus)
1. Đi đến **Configuration** → **Data Sources**
2. Click **Add data source**
3. Chọn **Prometheus**
4. URL: `http://prometheus:9090`
5. Click **Save & Test**

### 3. Tạo Dashboard mới

#### Dashboard 1: Test Coverage
```
Queries:
- dronedelivery_test_coverage_statements
- dronedelivery_test_coverage_branches
- dronedelivery_test_coverage_functions
- dronedelivery_test_coverage_lines

Visualization: Gauge
```

#### Dashboard 2: Test Results
```
Queries:
- dronedelivery_tests_passed
- dronedelivery_tests_failed
- dronedelivery_tests_total

Visualization: Stat
```

### 4. Import Dashboard từ JSON

```json
{
  "dashboard": {
    "title": "DroneDelivery Test Metrics",
    "panels": [
      {
        "title": "Test Coverage - Statements",
        "targets": [
          {
            "expr": "dronedelivery_test_coverage_statements"
          }
        ],
        "type": "gauge"
      },
      {
        "title": "Test Coverage - Branches",
        "targets": [
          {
            "expr": "dronedelivery_test_coverage_branches"
          }
        ],
        "type": "gauge"
      },
      {
        "title": "Test Results",
        "targets": [
          {
            "expr": "dronedelivery_tests_passed",
            "legendFormat": "Passed"
          },
          {
            "expr": "dronedelivery_tests_failed",
            "legendFormat": "Failed"
          }
        ],
        "type": "piechart"
      }
    ]
  }
}
```

---

## 🚀 Các lệnh hữu ích

### Chạy lại test
```powershell
cd D:\cnpm\CNPM-3\CNPM\DroneDelivery-main\BackEnd
npm run test:unit          # Chạy unit tests
npm run test:integration   # Chạy integration tests
npm run test:watch         # Chạy test ở chế độ watch
npm test                   # Chạy tất cả tests
```

### Export metrics
```powershell
cd D:\cnpm\CNPM-3\CNPM
python .\scripts\export_dronedelivery_metrics.py
```

### Restart metrics-server
```powershell
docker compose restart metrics-server
```

### Kiểm tra Prometheus targets
```
http://localhost:9090/targets
```

---

## 📝 Ghi chú

- Metrics được export tại: `monitoring/metrics/dronedelivery_test_metrics.txt`
- Prometheus scrape metrics từ `metrics-server:8000`
- Grafana auto-refresh mỗi 30 giây
- Các metrics được cập nhật mỗi khi chạy test

---

## ⚠️ Troubleshooting

### Metrics không hiển thị trong Grafana
1. Kiểm tra Prometheus targets: `http://localhost:9090/targets`
2. Restart metrics-server: `docker compose restart metrics-server`
3. Kiểm tra logs: `docker compose logs metrics-server`

### Test failures
- 1 test failed ở `order.model.test.js` (Status field undefined)
- Cần fix order schema hoặc test setup

