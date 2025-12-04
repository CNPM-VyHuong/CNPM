# 🎯 Quick Start - DroneDelivery Testing

## Bước 1: Cài đặt Dependencies

```powershell
cd DroneDelivery-main\BackEnd
npm install
```

## Bước 2: Chạy Tests

```powershell
# Trong thư mục DroneDelivery-main\BackEnd
npm test
```

Hoặc sử dụng test runner script:

```powershell
# Từ thư mục gốc CNPM
python .\scripts\run_dronedelivery_tests.py .\DroneDelivery-main
```

## Bước 3: Cập nhật Grafana

```powershell
# Restart metrics-server để load metrics mới
docker compose restart metrics-server

# Đợi 30 giây
Start-Sleep -Seconds 30
```

## Bước 4: Xem Kết Quả

Mở Grafana: **http://localhost:3001** (admin/admin)

### Dashboard sẽ hiển thị:

#### 🚁 DroneDelivery Panels:
1. **Test Pass Rate** - Gauge showing % tests passed
2. **Test Results** - Time series (Total, Passed, Failed)
3. **Code Coverage** - Bar gauge (Statements, Branches, Functions, Lines)
4. **HTTP Request Rate** - Request rate by endpoint

#### ☕ Java Backend Panels:
- Test results by service
- Test execution time
- Pass/fail rates

---

## 📊 Test Structure

### Unit Tests (3 files):
- `__tests__/unit/user.model.test.js` - User model (8 tests)
- `__tests__/unit/order.model.test.js` - Order model (10 tests)
- `__tests__/unit/drone.model.test.js` - Drone model (10 tests)

### Integration Tests (3 files):
- `__tests__/integration/auth.api.test.js` - Auth API (6 tests)
- `__tests__/integration/order.api.test.js` - Order API (5 tests)
- `__tests__/integration/drone.api.test.js` - Drone API (7 tests)

**Total: ~46 test cases**

---

## 🔧 Troubleshooting

### Tests không chạy?
```powershell
# Kiểm tra node_modules
cd DroneDelivery-main\BackEnd
npm install

# Chạy lại tests
npm test
```

### Metrics không hiện trong Grafana?
```powershell
# 1. Kiểm tra metrics file
cat .\monitoring\metrics\dronedelivery_test_metrics.txt

# 2. Restart metrics-server
docker compose restart metrics-server

# 3. Kiểm tra Prometheus
# Mở http://localhost:9090
# Query: dronedelivery_test_total
```

### Coverage không đủ?
```powershell
# Xem coverage report
cd DroneDelivery-main\BackEnd
npm test
# Mở: coverage/lcov-report/index.html
```

---

## ✅ Checklist

- [ ] `npm install` trong DroneDelivery-main\BackEnd
- [ ] `npm test` chạy thành công
- [ ] File `dronedelivery_test_metrics.txt` được tạo
- [ ] `docker compose restart metrics-server` đã chạy
- [ ] Grafana hiển thị DroneDelivery panels
- [ ] Metrics không bị mất khi restart

---

**🎉 Hoàn tất! Bây giờ bạn có thể quản lý DroneDelivery trong Grafana cùng với các backend services khác!**
