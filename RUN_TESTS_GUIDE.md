# 🧪 Single Service Test Runner Guide

Chạy test từng project riêng biệt và hiển thị kết quả real-time trong Grafana dashboard.

## 🎯 Features

- ✅ Chạy test cho **một project** hoặc **tất cả projects**
- ✅ **Real-time dashboard updates** (Grafana tự động refresh)
- ✅ Hiển thị metrics **từng service** trong dashboard
- ✅ JSON reports cho mỗi service
- ✅ Dễ sử dụng - click file .cmd hoặc chạy Python script

## 📋 Quick Start

### **Option 1: Windows - Sử Dụng .cmd Files (Dễ Nhất)**

#### Chạy Test Cho Một Project:
```cmd
# Cách 1: Double-click file
run_test.cmd user_service

# Cách 2: Command line
cd D:\cnpm\CNPM-3
run_test.cmd product_service
```

#### Chạy Test Tất Cả Projects:
```cmd
# Double-click file
run_all_tests.cmd

# Hoặc command line
cd D:\cnpm\CNPM-3
run_all_tests.cmd
```

### **Option 2: Command Line - Python Scripts**

#### Chạy Test Một Project:
```bash
python scripts/run_single_service_test.py D:\cnpm\CNPM-3\DoAnCNPM_Backend user_service
```

#### Chạy Test Tất Cả Projects:
```bash
python scripts/run_all_services_test.py D:\cnpm\CNPM-3\DoAnCNPM_Backend
```

## 🎮 Các Project Có Sẵn

| Service | Location |
|---------|----------|
| **user_service** | DoAnCNPM_Backend/user_service |
| **product_service** | DoAnCNPM_Backend/product_service |
| **drone_service** | DoAnCNPM_Backend/drone_service |
| **order_service** | DoAnCNPM_Backend/order_service |
| **payment_service** | DoAnCNPM_Backend/payment_service |
| **restaurant-service** | DoAnCNPM_Backend/restaurant-service |

## 📊 Grafana Dashboard Integration

### **Cách Hoạt Động:**

```
1. Chạy: run_test.cmd user_service
   ↓
2. Script thực thi: mvn test -q
   ↓
3. Parse: TEST-*.xml reports
   ↓
4. Update: monitoring/metrics/test_metrics.txt
   ↓
5. Prometheus scrape metrics (sau ~30s)
   ↓
6. Grafana display latest data
   ↓
Dashboard auto-refresh: http://localhost:3001
```

### **Dashboard Panels Được Update:**

1. **Test Results by Service** (Table)
   - Columns: Service, Passed, Failed, Total
   - Auto-filter theo service label

2. **Success Rate by Service %** (Gauge)
   - Shows pass rate per service
   - Color coded: Green (>95%), Yellow (>80%), Red (<80%)

3. **Test Execution Time** (Time Series)
   - Shows execution time trend per service

4. **Test Rate Trends** (Line Chart)
   - Total/Pass/Fail counts over time

## 📈 Metrics Generated

### **Per-Service Metrics:**
```promql
test_count_by_service{service="user_service"}
test_pass_count_by_service{service="user_service"}
test_fail_count_by_service{service="user_service"}
test_pass_rate_by_service{service="user_service"}
test_execution_time_by_service{service="user_service"}
```

### **Summary Metrics:**
```promql
test_count_total              # Total all tests
test_pass_count              # Total passed
test_fail_count              # Total failed
test_pass_rate_percent       # Overall pass rate
test_execution_time_seconds  # Total time
```

## 📁 Output Files

### **After running tests:**

```
monitoring/metrics/
├── test_metrics.txt                    # Prometheus format (Grafana reads this)
├── test_report_user_service.json       # Individual service report
├── test_report_product_service.json
├── test_report_drone_service.json
└── ... (một file cho mỗi service)
```

### **Example test_report_user_service.json:**
```json
{
  "service": "user_service",
  "timestamp": "2025-11-26T10:30:45.123456",
  "metrics": {
    "total_tests": 48,
    "passed_tests": 48,
    "failed_tests": 0,
    "execution_time_seconds": 12.45,
    "pass_rate_percent": 100.0,
    "status": "PASS"
  }
}
```

## 🔄 Workflow Example

### **Scenario: Chạy test từng service**

```bash
# Terminal 1 - Chạy Prometheus
prometheus --config.file=monitoring/prometheus.yml

# Terminal 2 - Chạy Metrics Server
python monitoring/metrics-server.py

# Terminal 3 - Docker Grafana
docker-compose up grafana

# Terminal 4 - Chạy tests tuần tự
cd D:\cnpm\CNPM-3

# Test user_service
run_test.cmd user_service
# ✓ PASS: 48 tests passed

# Test product_service  
run_test.cmd product_service
# ✓ PASS: 27 tests passed

# Test order_service
run_test.cmd order_service
# ✓ PASS: 4 tests passed

# Xem Dashboard
# Open: http://localhost:3001
# → Bảng "Test Results by Service" hiển thị 3 services vừa chạy
```

## ⚙️ Configuration

### **Edit Service List** (scripts/run_all_services_test.py)
```python
self.services = [
    'user_service',
    'product_service',
    'drone_service',
    'order_service',
    'payment_service',
    'restaurant-service',
    # Thêm project mới ở đây
]
```

### **Edit Paths** (run_test.cmd)
```cmd
set "BACKEND_PATH=D:\cnpm\CNPM-3\DoAnCNPM_Backend"
set "SCRIPT_PATH=D:\cnpm\CNPM-3\scripts\run_single_service_test.py"
```

## 🐛 Troubleshooting

### **Q: Dashboard không update sau khi chạy test?**
**A:**
1. Check metrics file: `monitoring/metrics/test_metrics.txt`
2. Check Prometheus targets: http://localhost:9090/targets
3. Wait 30 seconds (Prometheus scrape interval)
4. Refresh Grafana: http://localhost:3001 (F5)

### **Q: "Maven not found" error?**
**A:**
- Cần có Maven installed: https://maven.apache.org/download.cgi
- Thêm Maven vào PATH

### **Q: Metrics file rỗng?**
**A:**
```bash
# Check test reports exist
dir D:\cnpm\CNPM-3\DoAnCNPM_Backend\user_service\target\surefire-reports\

# If empty, run tests manually
cd D:\cnpm\CNPM-3\DoAnCNPM_Backend\user_service
mvn test
```

### **Q: Cách xem logs?**
**A:**
```bash
# Xem JSON report
type monitoring\metrics\test_report_user_service.json

# Xem metrics raw
type monitoring\metrics\test_metrics.txt

# Xem Prometheus logs
docker-compose logs prometheus

# Xem Grafana logs
docker-compose logs grafana
```

## 📚 File Structure

```
D:\cnpm\CNPM-3\
├── run_test.cmd                          # ← Click để chạy 1 service
├── run_all_tests.cmd                     # ← Click để chạy tất cả
├── scripts/
│   ├── run_single_service_test.py        # Python script for 1 service
│   └── run_all_services_test.py          # Python script for all services
├── monitoring/
│   ├── metrics-server.py                 # Expose metrics endpoint
│   ├── prometheus.yml                    # Prometheus config
│   └── metrics/
│       ├── test_metrics.txt              # Prometheus format
│       └── test_report_*.json            # Individual reports
└── DoAnCNPM_Backend/
    ├── user_service/
    ├── product_service/
    ├── drone_service/
    ├── order_service/
    ├── payment_service/
    └── restaurant-service/
```

## 🎯 Best Practices

✅ **Do:**
- Chạy metrics-server & Prometheus trước khi test
- Chạy từng service một lần để verify
- Check dashboard sau mỗi batch test
- Commit metrics reports để track history

❌ **Don't:**
- Chạy tests trước khi metrics-server ready
- Modify test_metrics.txt manually
- Delete surefire-reports folders
- Run multiple tests cùng lúc (gây race condition)

## 🚀 Next Steps

1. **Setup Metrics Infrastructure:**
   ```bash
   docker-compose up -d
   ```

2. **Run Single Service Test:**
   ```cmd
   run_test.cmd user_service
   ```

3. **View Dashboard:**
   - Open: http://localhost:3001
   - Login: admin/admin
   - Go to: Services Dashboard

4. **Run All Tests:**
   ```cmd
   run_all_tests.cmd
   ```

5. **Monitor Progress:**
   - Watch test output in terminal
   - Watch dashboard update in real-time
   - Check JSON reports for details

---

**Version**: 1.0  
**Last Updated**: 2025-11-26  
**Author**: CNPM Team
