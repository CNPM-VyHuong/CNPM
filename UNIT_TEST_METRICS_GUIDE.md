# Unit Test Metrics với Grafana Dashboard

## 📊 Giới thiệu

Hệ thống giờ có thể **track test results** (pass/fail) từ unit tests và **hiển thị lên Grafana dashboard** real-time.

## 🎯 Cách hoạt động

```
Unit Tests (mvn test)
    ↓
Test Metrics (Micrometer)
    ↓
Prometheus (scrape metrics)
    ↓
Grafana Dashboard (visualize)
```

## 🚀 Quick Start

### 1. Chạy Docker Compose
```powershell
cd d:\cnpm\CNPM-3
docker-compose up -d
```

### 2. Chạy Tests (tùy chọn)
```powershell
# Chạy test của user-service
cd DoAnCNPM_Backend/user_service
mvn clean test

# Hoặc chạy tất cả services
cd DoAnCNPM_Backend
for ($service in 'user_service', 'product_service', 'order_service', 'payment_service') {
    cd $service
    mvn clean test
    cd ..
}
```

### 3. Xem kết quả trong Grafana
```
http://localhost:3001
User: admin
Pass: 1admin1
```

Chọn dashboard: **`CNPM K6 Performance Test Dashboard`**

## 📈 Metrics được track

| Metric | Mô tả |
|--------|-------|
| `tests.executed` | Tổng số test chạy |
| `tests.passed` | Số test pass |
| `tests.failed` | Số test fail |
| `test.result` | Chi tiết từng test (status, tên test) |
| `test.duration` | Thời gian chạy test (ms) |

## 🔧 Cấu hình

### Application Test Config
File: `src/test/resources/application-test.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  prometheus:
    metrics:
      export:
        enabled: true
```

### Prometheus Scrape Config
File: `monitoring/prometheus.yml`

```yaml
- job_name: "unit-tests"
  metrics_path: "/actuator/prometheus"
  scrape_interval: 10s
  static_configs:
    - targets: ["localhost:8081", "localhost:8088", ...]
  metric_relabel_configs:
    - source_labels: [__name__]
      regex: 'tests_.*'
      action: keep
```

## 📝 Cách thêm metrics vào test cũ

### Cách 1: Extend BaseTest (Easy)
```java
// Before
public class UserServiceUnitTest { ... }

// After
public class UserServiceUnitTest extends BaseTest { ... }
```

### Cách 2: Inject MeterRegistry (Manual)
```java
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void testSomething() {
        // Test code
        meterRegistry.counter("custom.metric").increment();
    }
}
```

### Cách 3: Dùng TestMetricsTracker (Auto)
```java
@SpringBootTest
@ExtendWith(TestMetricsExtension.class)
public class UserServiceTest { ... }
```

## 🎨 Grafana Dashboard Panels

Dashboard tự động hiển thị:
- ✅ **Test Success Rate %** - Phần trăm test pass
- ✅ **Response Time (ms)** - Tốc độ chạy test
- ✅ **Tests Passed vs Failed** - Biểu đồ so sánh
- ✅ **Error Rate** - Tỷ lệ lỗi
- ✅ **Total Successful Tests** - Tổng number pass
- ✅ **Total Failed Tests** - Tổng number fail

## 🔍 Debugging

### Xem Prometheus metrics
```
http://localhost:9090/targets
```

### Kiểm tra service metrics
```bash
curl http://localhost:8081/actuator/prometheus | grep tests_
```

### Docker logs
```powershell
# View Prometheus logs
docker logs -f prometheus

# View Grafana logs
docker logs -f grafana

# View service logs
docker logs -f user-service
```

## 📚 Các test hiện tại

Các test files hiện tại sẽ tự động track metrics:

```
DoAnCNPM_Backend/
├── user_service/src/test/java/
│   ├── UserServiceUnitTest.java
│   ├── AuthServiceUnitTest.java
│   └── UserControllerUnitTest.java
├── product_service/
│   ├── ProductServiceTest.java
│   ├── ProductServiceExceptionTest.java
│   └── ProductControllerTest.java
├── order_service/
│   ├── OrderServiceUnitTest.java
│   └── OrderServiceIntegrationTest.java
└── ...
```

## 💡 Tips

1. **Test chạy lâu?** → Check docker-compose resources
2. **Metrics không hiện?** → Restart Prometheus: `docker restart prometheus`
3. **Dashboard trống?** → Chạy test trước: `mvn test`
4. **Muốn xem raw metrics?** → Query Prometheus: `curl http://localhost:9090/api/v1/query?query=tests_passed`

## 🚀 Next Steps

- [ ] Add CI/CD pipeline để auto-run tests
- [ ] Setup email alerts cho test failures
- [ ] Add code coverage metrics
- [ ] Create performance baseline thresholds
- [ ] Setup test result trending

---

**Created:** November 23, 2025
**Status:** ✅ Ready for use
