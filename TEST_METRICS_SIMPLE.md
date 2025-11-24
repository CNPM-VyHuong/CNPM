# 📊 Unit Test Results in Grafana - Quick Start Guide

## Quy Trình Làm Việc

```
Chạy Tests → Generate JUnit XML → Parse XML → Prometheus Metrics → Grafana Dashboard
```

## Cách Sử Dụng

### **1️⃣ Chạy Tests và Export Metrics**

**Option A: Chạy tất cả services**
```bash
cd d:\cnpm\CNPM-3
bash scripts/run-tests-and-export.sh
```

**Option B: Chạy một service**
```bash
cd d:\cnpm\CNPM-3\DoAnCNPM_Backend\user_service
mvn clean test
cd ../..
python3 scripts/parse-junit-to-prometheus.py
```

### **2️⃣ Khởi động Infrastructure**

```bash
# Terminal 1: Khởi động Prometheus, Grafana, etc.
docker-compose up -d

# Terminal 2: Khởi động metrics server
python3 scripts/serve-metrics.py
```

### **3️⃣ Xem Results trong Grafana**

1. Truy cập: http://localhost:3001
2. Đăng nhập: `admin` / `1admin1`
3. Mở dashboard: **Unit Test Results**

## 📈 Các Metrics Hiển Thị

| Metric | Ý Nghĩa |
|--------|---------|
| `unit_tests_total` | Tổng số tests |
| `unit_tests_passed` | Số tests pass ✅ |
| `unit_tests_failed` | Số tests fail ❌ |
| `unit_tests_errors` | Số lỗi 🔴 |
| `unit_tests_skipped` | Số tests skip ⏭️ |
| `unit_tests_duration_seconds` | Thời gian chạy ⏱️ |
| `unit_tests_success_rate` | % thành công 📊 |

## 🏗️ Tệp Được Tạo

```
├── scripts/
│   ├── parse-junit-to-prometheus.py    # Parse JUnit XML → Prometheus metrics
│   ├── serve-metrics.py                # HTTP server expose metrics (port 9091)
│   └── run-tests-and-export.sh         # Wrapper: chạy tests + export
├── monitoring/
│   ├── metrics/
│   │   └── unit-tests.prom             # Metrics file (tự động tạo)
│   └── prometheus.yml                  # Updated với unit-tests job
└── monitoring/grafana/provisioning/dashboards/
    └── unit-tests-dashboard.json       # Dashboard 6 panels
```

## 🔧 Workflow Chi Tiết

### **Step 1: Parse Test Results**
```python
# parse-junit-to-prometheus.py
- Tìm tất cả JUnit XML files: target/surefire-reports/*.xml
- Extract: tests, failures, errors, skipped, duration
- Tính: success rate = (passed/total) * 100
- Output: monitoring/metrics/unit-tests.prom
```

### **Step 2: Expose Metrics**
```python
# serve-metrics.py trên port 9091
- GET /metrics → đọc unit-tests.prom
- Format: Prometheus text format
- Prometheus scrape: http://localhost:9091/metrics mỗi 30s
```

### **Step 3: Prometheus Query**
```
# prometheus.yml
job_name: "unit-tests"
  targets: ["localhost:9091"]
  scrape_interval: 30s
```

### **Step 4: Grafana Visualization**
```json
// unit-tests-dashboard.json
6 panels:
1. Tests Passed (line chart)
2. Tests Failed (stat)
3. Success Rate % (gauge)
4. Execution Time (bar chart)
5. Total Tests (stacked bars)
6. Test Errors (line chart)
```

## 📝 Example Output

**Sau khi chạy tests:**
```
✓ Parsed user_service: 8/8 passed
✓ Parsed product_service: 5/5 passed
✓ Parsed order_service: 6/6 passed
✓ Parsed payment_service: 4/4 passed
✓ Metrics written to monitoring/metrics/unit-tests.prom
```

**Nội dung unit-tests.prom:**
```
# HELP unit_tests_total Total number of unit tests
# TYPE unit_tests_total gauge
unit_tests_total{service="user_service"} 8 1700000000000

# HELP unit_tests_passed Number of passed tests
# TYPE unit_tests_passed gauge
unit_tests_passed{service="user_service"} 8 1700000000000

# HELP unit_tests_success_rate Test success rate percentage
# TYPE unit_tests_success_rate gauge
unit_tests_success_rate{service="user_service"} 100.0 1700000000000
```

## ✅ Checklist

- [ ] Tests chạy thành công (JUnit XML generated)
- [ ] Parse script chạy: `python3 scripts/parse-junit-to-prometheus.py`
- [ ] Metrics file tạo: `monitoring/metrics/unit-tests.prom`
- [ ] Metrics server khởi động: `python3 scripts/serve-metrics.py`
- [ ] Prometheus scrape thành công (check http://localhost:9090)
- [ ] Grafana dashboard hiển thị (http://localhost:3001)

## 🐛 Troubleshooting

**Q: Metrics không hiển thị trong Grafana?**
- A: Check Prometheus targets: http://localhost:9090/targets
- Ensure metrics-server running: `docker ps | grep metrics-server`
- Check metrics file exists: `cat monitoring/metrics/unit-tests.prom`

**Q: Metrics file rỗng?**
- A: Check JUnit XML files generated: `find . -name "*.xml" -path "*surefire*"`
- Run parse script with debug: `python3 scripts/parse-junit-to-prometheus.py`

**Q: Port 9091 đang sử dụng?**
- A: Kill process: `netstat -ano | findstr 9091` (Windows)
- Hoặc change port trong serve-metrics.py

## 🚀 Tự động hóa (Optional)

Thêm vào CI/CD pipeline (.github/workflows/ci-cd.yml):
```yaml
- name: Run tests and export metrics
  run: bash scripts/run-tests-and-export.sh
```

## 📚 Tham Khảo

- [Prometheus Text Format](https://prometheus.io/docs/instrumenting/exposition_formats/)
- [Grafana Dashboard JSON Model](https://grafana.com/docs/grafana/latest/dashboards/)
- [Maven Surefire Report Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
