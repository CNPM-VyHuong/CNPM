# 🚁 DroneDelivery Testing & Monitoring Setup Guide

## 📋 Tổng quan

Hệ thống đã được cấu hình với:
- ✅ Jest testing framework (unit + integration tests)
- ✅ Prometheus metrics monitoring
- ✅ Grafana dashboard integration
- ✅ Test runner script tự động

## 🔧 Cài đặt Dependencies

### 1. Cài đặt npm packages

```powershell
cd DroneDelivery-main\BackEnd
npm install
```

Các packages chính đã được thêm vào `package.json`:
- `prom-client` - Prometheus metrics
- `jest`, `babel-jest`, `@babel/core`, `@babel/preset-env` - Testing framework
- `supertest` - API testing
- `mongodb-memory-server` - In-memory MongoDB cho tests
- `cross-env` - Cross-platform environment variables

## 🧪 Chạy Tests

### Chạy tất cả tests với coverage:
```powershell
cd DroneDelivery-main\BackEnd
npm test
```

### Chạy chỉ unit tests:
```powershell
npm run test:unit
```

### Chạy chỉ integration tests:
```powershell
npm run test:integration
```

### Watch mode (tự động chạy lại khi code thay đổi):
```powershell
npm run test:watch
```

## 📊 Test Structure

```
DroneDelivery-main/BackEnd/
├── __tests__/
│   ├── setup.js                          # Test configuration
│   ├── unit/
│   │   ├── user.model.test.js           # User model tests
│   │   ├── order.model.test.js          # Order model tests
│   │   └── drone.model.test.js          # Drone model tests
│   └── integration/
│       ├── auth.api.test.js             # Auth API tests
│       ├── order.api.test.js            # Order API tests
│       └── drone.api.test.js            # Drone API tests
├── jest.config.js                        # Jest configuration
├── .babelrc                              # Babel configuration
└── package.json                          # Updated with test scripts
```

## 📈 Metrics & Monitoring

### 1. DroneDelivery Backend Metrics

Metrics endpoint: `http://localhost:5000/metrics`

Available metrics:
- `http_request_duration_seconds` - Request latency
- `http_requests_total` - Total requests
- `active_connections` - Active Socket.io connections
- `drone_status` - Drone status by type

### 2. Test Metrics

Script tự động: `scripts/run_dronedelivery_tests.py`

```powershell
python .\scripts\run_dronedelivery_tests.py .\DroneDelivery-main
```

Metrics được export:
- `dronedelivery_test_total` - Total tests
- `dronedelivery_test_passed` - Passed tests
- `dronedelivery_test_failed` - Failed tests
- `dronedelivery_test_pass_rate` - Pass rate %
- `dronedelivery_coverage_statements` - Statement coverage
- `dronedelivery_coverage_branches` - Branch coverage
- `dronedelivery_coverage_functions` - Function coverage
- `dronedelivery_coverage_lines` - Line coverage

## 🎯 Grafana Dashboard

Dashboard đã được cập nhật với DroneDelivery panels:

### Panels mới:
1. **🚁 DroneDelivery - Test Pass Rate** (Gauge)
   - Hiển thị % tests passed

2. **🚁 DroneDelivery - Test Results** (Time series)
   - Total tests, Passed, Failed over time

3. **🚁 DroneDelivery - Code Coverage** (Bar gauge)
   - Statements, Branches, Functions, Lines coverage

4. **🚁 DroneDelivery - HTTP Request Rate** (Time series)
   - Request rate by method and route

### Truy cập dashboard:
```
URL: http://localhost:3001
Username: admin
Password: admin
```

## 🔄 Workflow Complete

### Chạy tests và cập nhật Grafana:

```powershell
# 1. Chạy DroneDelivery tests
python .\scripts\run_dronedelivery_tests.py .\DroneDelivery-main

# 2. Restart metrics-server để load metrics mới
docker compose restart metrics-server

# 3. Đợi 30 giây để Prometheus scrape
Start-Sleep -Seconds 30

# 4. Mở Grafana và xem kết quả
# http://localhost:3001
```

## 📝 Test Examples

### Unit Test Example (User Model):
```javascript
it('should create a new user with valid data', async () => {
  const userData = {
    name: 'John Doe',
    email: 'john@example.com',
    password: 'password123',
    phoneNumber: '0123456789',
  };
  
  const user = new User(userData);
  const savedUser = await user.save();
  
  expect(savedUser._id).toBeDefined();
  expect(savedUser.email).toBe(userData.email);
});
```

### Integration Test Example (Auth API):
```javascript
it('should register a new user successfully', async () => {
  const userData = {
    name: 'John Doe',
    email: 'john@example.com',
    password: 'Password123!',
    phoneNumber: '0123456789',
  };
  
  const response = await request(app)
    .post('/api/auth/signup')
    .send(userData)
    .expect('Content-Type', /json/);
    
  expect(response.status).toBe(201);
});
```

## 🎉 Summary

Hệ thống DroneDelivery đã được tích hợp hoàn toàn vào monitoring stack:

✅ **Testing**:
- 3 Unit test files (User, Order, Drone models)
- 3 Integration test files (Auth, Order, Drone APIs)
- Total ~30+ test cases

✅ **Monitoring**:
- Prometheus metrics cho backend performance
- Test metrics tự động export
- Grafana dashboard với 4 panels mới

✅ **Automation**:
- Test runner script Python
- Metrics auto-update với timestamp hiện tại
- Persistent metrics trong Prometheus

## 🚀 Next Steps

1. Cài đặt npm dependencies: `npm install`
2. Chạy tests: `npm test`
3. Run test runner script: `python .\scripts\run_dronedelivery_tests.py .\DroneDelivery-main`
4. Restart metrics-server: `docker compose restart metrics-server`
5. Xem kết quả trong Grafana: http://localhost:3001

---

**Note**: Metrics sẽ KHÔNG bị mất khi restart - tất cả được lưu trong Prometheus database với timestamp tự động cập nhật! 🎯
