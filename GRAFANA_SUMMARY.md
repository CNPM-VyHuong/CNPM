# 📊 Grafana - Giải pháp Quản lý Hệ thống

## **Grafana là gì?**
Một **nền tảng giám sát tập trung (Centralized Monitoring)** - Quản lý **TOÀN BỘ hệ thống** (không chỉ test) thông qua **dashboard trực quan, real-time, tự động cập nhật**.

---

## **Công dụng chính**

| Chức năng | Mô tả | Ví dụ |
|-----------|-------|-------|
| **1. Test Monitoring** | Theo dõi unit tests | 48 tests, 48 passed, 0 failed, 100% pass rate |
| **2. Service Health** | Kiểm tra dịch vụ hoạt động | Eureka, API Gateway, 6 microservices có lên không |
| **3. Resource Usage** | Giám sát tài nguyên | CPU, Memory, Disk mỗi container |
| **4. API Performance** | Phân tích hiệu suất API | Response time, throughput (req/sec) |
| **5. Database Monitoring** | Theo dõi database | PostgreSQL, MongoDB kết nối được không, query chậy không |
| **6. Real-time Alerts** | Cảnh báo tự động | CPU > 80%, Memory > 90%, Service down → gửi email |
| **7. Historical Analytics** | Phân tích lịch sử | So sánh hiệu suất hôm nay vs tuần trước |
| **8. Centralized Dashboard** | Quản lý tập trung | Một chỗ xem tất cả thông tin |

---

## **Trong dự án của chúng ta - Quản lý gì?**

### **Dashboard: Services Monitoring**

Hiển thị **5 nhóm thông tin**:

#### **Nhóm 1: 🧪 Test Quality (Chất lượng Test)**
- **✅ Test Results by Service** - Bảng: Service | Passed | Failed | Total
- **📊 Success Rate by Service %** - % test pass mỗi service
- **📈 Test Rate Trends** - Xu hướng passed/failed theo thời gian
- **⏱️ Test Execution Time** - Thời gian chạy tests
- **→ Cho biết:** Code chất lượng, có bug không, stable không

#### **Nhóm 2: 🖥️ Infrastructure Health (Sức khỏe Hệ thống)** 
- **CPU Usage** - CPU container dùng bao nhiêu % (nếu có)
- **Memory Usage** - RAM container dùng bao nhiêu MB (nếu có)
- **Disk Usage** - Disk đầy bao nhiêu % (nếu có)
- **→ Cho biết:** Container có bị overload không, cần scale up không

#### **Nhóm 3: 🌐 Service Availability (Dịch vụ hoạt động)**
- **Service Status** - Eureka, API Gateway, 6 microservices up/down
- **Uptime** - Dịch vụ chạy bao lâu rồi
- **→ Cho biết:** Có service nào bị down không, cần restart không

#### **Nhóm 4: ⚡ API Performance (Hiệu suất API)**
- **Response Time** - API trả response trong bao lâu (ms)
- **Throughput** - Bao nhiêu request/second
- **Error Rate** - Bao nhiêu % request bị lỗi
- **→ Cho biết:** API chạy nhanh không, có bottleneck không, có lỗi không

#### **Nhóm 5: 🗄️ Data Layer (Cơ sở dữ liệu)**
- **Database Connection** - PostgreSQL, MongoDB kết nối được không
- **Query Performance** - Query chạy bao lâu
- **Data Volume** - Dữ liệu có bao nhiêu record
- **→ Cho biết:** Database hoạt động bình thường không, có vấn đề không

---

---

## **Lợi ích của Grafana**

| Lợi ích | Chi tiết | Ý nghĩa |
|---------|---------|---------|
| **👁️ Centralized View** | Một chỗ xem tất cả | Không cần chạy 10 lệnh khác nhau |
| **⚡ Real-time** | Cập nhật mỗi 5-15s | Biết vấn đề ngay lập tức |
| **📊 Visual** | Biểu đồ, bảng, gauge | Dễ hiểu hơn console logs |
| **📈 Historical** | Lưu dữ liệu 30 ngày | Phân tích xu hướng, detect regression |
| **🚨 Alerting** | Cảnh báo tự động | Không cần mở Grafana mỗi lúc |
| **👥 Shared** | Team cùng truy cập | Management, DevOps, Dev cùng xem |
| **🔧 Customizable** | Tạo dashboard tùy ý | Tạo dashboard riêng cho từng team |

---

## **Ví dụ Sử dụng (Use Cases)**

### **Scenario 1: Phát hiện Regression**
```
Hôm nay mở Grafana:
- Test Results: 48 tests, 46 passed, 2 failed ❌
- Yesterday: 48 tests, 48 passed ✅

→ Phát hiện: Có 2 test fail
→ Action: Tìm commits mới nhất, revert hoặc fix
```

### **Scenario 2: Tìm Performance Bottleneck**
```
API Response Time:
- User Service: 50ms (OK)
- Order Service: 200ms (SLOW) ⚠️
- Product Service: 100ms (OK)

→ Phát hiện: Order Service chậm
→ Action: Optimize database query, add caching
```

### **Scenario 3: Resource Planning**
```
Memory Usage:
- Week 1: 30% (safe)
- Week 2: 50% (warning)
- Week 3: 70% (critical)

→ Phát hiện: Memory tăng từng tuần
→ Action: Scale up resource hoặc optimize code
```

### **Scenario 4: Maintenance Alert**
```
Database Down Alert!
→ Prometheus detects PostgreSQL port 5433 unreachable
→ Grafana sends alert to Slack/Email
→ DevOps nhận được ngay và fix

(Thay vì user báo "app bị lỗi")
```  

---

## **Cách lấy dữ liệu**

### **1️⃣ Nguồn dữ liệu (Data Sources)**

| Nguồn | Cách lấy | Dữ liệu |
|-------|----------|--------|
| **Chạy Tests** | `mvn test` → JUnit XML | test_pass_count, test_fail_count |
| **Parse Metrics** | `test_metrics_parser.py` → convert XML | test_metrics.txt (Prometheus format) |
| **Metrics Server** | Python HTTP server (port 9091) | Expose metrics via `/metrics` endpoint |
| **Prometheus** | Scrape `/metrics` mỗi 15s | Lưu trữ time-series data |
| **Grafana** | Query Prometheus | Vẽ biểu đồ từ data |

### **2️⃣ Quy trình chi tiết**

```
1. Chạy Tests
   ↓
   mvn test → Tạo TEST-*.xml (JUnit format)
   
2. Parse Metrics
   ↓
   test_metrics_parser.py đọc XML files
   → Trích xuất: test_count, pass_count, fail_count, execution_time
   → Ghi vào: test_metrics.txt (Prometheus format)
   
3. Expose Metrics
   ↓
   metrics-server (Python HTTP) chạy trên port 9091
   → Serve test_metrics.txt trên /metrics endpoint
   
4. Prometheus Scrapes
   ↓
   Prometheus config → Scrape http://metrics-server:9091/metrics
   → Lưu trữ metrics trong time-series database
   
5. Grafana Queries
   ↓
   Dashboard queries Prometheus:
   - "test_pass_count_by_service" 
   - "test_fail_count_by_service"
   - "test_count_by_service"
   
6. Visualization
   ↓
   Grafana vẽ biểu đồ → Browser hiển thị
```

### **3️⃣ Cách hoạt động (Flow)**

```
┌──────────────────────────────────────────────────────────────┐
│                   1. Test Execution                          │
├──────────────────────────────────────────────────────────────┤
│  Product Service:  27 tests → 27 passed, 0 failed, 5.16s    │
│  Drone Service:    13 tests → 13 passed, 0 failed, 47.91s   │
│  Order Service:     4 tests →  4 passed, 0 failed, 24.72s   │
│  Payment Service:   4 tests →  4 passed, 0 failed, 31.31s   │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│          2. JUnit XML Files (trong target/surefire-reports)  │
├──────────────────────────────────────────────────────────────┤
│  TEST-ProductServiceTest.xml  (27 test cases)               │
│  TEST-DroneServiceTest.xml    (13 test cases)               │
│  TEST-OrderServiceTest.xml    (4 test cases)                │
│  TEST-PaymentServiceTest.xml  (4 test cases)                │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│     3. Parse (test_metrics_parser.py)                        │
├──────────────────────────────────────────────────────────────┤
│  Đọc XML → Trích xuất metrics → Format Prometheus           │
│  Output: test_metrics.txt                                    │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│     4. Metrics File (test_metrics.txt)                       │
├──────────────────────────────────────────────────────────────┤
│  test_count_by_service{service="product_service"} 27        │
│  test_pass_count_by_service{service="product_service"} 27   │
│  test_fail_count_by_service{service="product_service"} 0    │
│  test_execution_time_by_service{service="product_service"}  │
│                                                   5.16       │
│  ... (tương tự cho drone, order, payment)                   │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│     5. Metrics Server (port 9091)                            │
├──────────────────────────────────────────────────────────────┤
│  HTTP GET /metrics → Trả về file test_metrics.txt           │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│     6. Prometheus (port 9090)                                │
├──────────────────────────────────────────────────────────────┤
│  Scrape config: job_name "unit-tests"                        │
│  targets: ["metrics-server:9091"]                            │
│  → Query /metrics mỗi 30 giây                                │
│  → Lưu trữ time-series data                                  │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│     7. Grafana (port 3001)                                   │
├──────────────────────────────────────────────────────────────┤
│  Query Prometheus:                                           │
│  - test_pass_count_by_service                               │
│  - test_fail_count_by_service                               │
│  - test_count_by_service                                    │
│  - test_execution_time_by_service                           │
│  - test_pass_rate_by_service                                │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│     8. Dashboard Visualization                               │
├──────────────────────────────────────────────────────────────┤
│  Panel 1: Test Results (Bảng)                               │
│  Service | Passed | Failed | Total                          │
│  product |   27   |   0    |  27                            │
│  drone   |   13   |   0    |  13                            │
│  order   |    4   |   0    |   4                            │
│  payment |    4   |   0    |   4                            │
│                                                              │
│  Panel 2: Success Rate (Bảng)                               │
│  Service | Success Rate %                                   │
│  product |     100%                                         │
│  drone   |     100%                                         │
│  order   |     100%                                         │
│  payment |     100%                                         │
│                                                              │
│  Panel 3: Test Trends (Line Chart)                          │
│  Xu hướng passed/failed theo thời gian                      │
│                                                              │
│  Panel 4: Execution Time (Bar Chart)                        │
│  drone: 47.91s, product: 5.16s, ...                        │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│            9. Browser Display                                │
├──────────────────────────────────────────────────────────────┤
│  http://localhost:3001/d/fastfood-services                  │
│  → Hiển thị 4 panels trên                                    │
└──────────────────────────────────────────────────────────────┘
```

---

## **Dữ liệu từ đó cho biết điều gì? (Insights)**

### **✅ Test Results by Service**
**Dữ liệu hiển thị:** Service, Passed, Failed, Total

**Cho biết:**
- ✅ **Chất lượng code** - Nếu failed > 0 → có bug cần fix
- ✅ **Regression test** - Hôm nay pass nhưng ngày mai fail → code có vấn đề
- ✅ **Coverage mỗi service** - Cái nào có test nhiều, cái nào chưa
- ✅ **Stability** - 100% pass = code ổn định, 90% = có vấn đề

**Ví dụ:**
```
Product Service: 27 passed, 0 failed → ✅ Code chất lượng
Drone Service:   13 passed, 0 failed → ✅ Code chất lượng
User Service:     0 passed, 0 failed → ⚠️ Chưa có test
```

### **📊 Success Rate by Service %**
**Dữ liệu hiển thị:** Tỷ lệ % = (Passed / Total) × 100

**Cho biết:**
- **Reliability** - Bao nhiêu % test chạy thành công
- **Risk** - Nếu < 90% → có nguy cơ bug
- **Confidence** - 100% = đủ tự tin deploy, 80% = cần kiểm tra thêm

**Ví dụ:**
```
100% = ✅ Safe to deploy
 90% = ⚠️ Check before deploying
 70% = ❌ Do NOT deploy
```

### **📈 Test Rate Trends**
**Dữ liệu hiển thị:** Biểu đồ line (Total/Passed/Failed) theo thời gian

**Cho biết:**
- **Regression** - Hôm nay failed tăng → có vấn đề mới
- **Improvement** - Tuần trước pass 20, hôm nay 30 → test coverage tốt hơn
- **Stability** - Nếu đường cong ổn định → hệ thống ổn định
- **Xu hướng** - Code đang tốt hay xấu hơn

**Ví dụ:**
```
Hôm nay:   48 passed ✅
Tuần trước: 48 passed ✅
→ Code chất lượng ổn định

Hôm nay:   45 passed (3 failed) ⚠️
Tuần trước: 48 passed ✅
→ Có regression, cần tìm bug
```

### **⏱️ Test Execution Time**
**Dữ liệu hiển thị:** Thời gian chạy (giây) mỗi service

**Cho biết:**
- **Performance** - Cái nào chạy nhanh, cái nào chạy chậm
- **Bottleneck** - Drone (47.91s) chậm → cần optimize
- **Efficiency** - Product (5.16s) nhanh → code hiệu quả
- **CI/CD time** - Tổng thời gian build & test bao lâu (108.82s)

**Ví dụ:**
```
Drone:   47.91s (chậm) → Có database query chậm?
Product:  5.16s (nhanh) → Code efficient
Total:  108.82s → CI/CD pipeline mất ~2 phút
```

---

## **Tổng hợp: Dữ liệu nói lên điều gì**

| Metric | Giá trị | Ý nghĩa |
|--------|--------|---------|
| **Total Tests** | 48 | Có 48 test cases cho 4 services |
| **Passed** | 48 | Tất cả test đều pass ✅ |
| **Failed** | 0 | Không có test fail ✅ |
| **Pass Rate** | 100% | Code chất lượng cao ✅ |
| **Exec Time** | 108.82s | CI/CD pipeline mất 2 phút |
| **Trend** | Ổn định | Không có regression ✅ |

**Kết luận:** 
- ✅ **Code chất lượng** - 100% pass rate
- ✅ **Ổn định** - Không có regression
- ⚠️ **Cần optimize** - Drone service chạy lâu (47.91s)
- ⚠️ **Chưa hoàn chỉnh** - User & Restaurant service chưa có test

---

🔗 **http://localhost:3001**
- Username: `admin`
- Password: `1admin1`
- Dashboard: **Services Monitoring** (FastFood Services)

---

---

## **🎯 TÓM LẠI**

### **Grafana = Trung tâm điều khiển hệ thống (Control Center)**

Như **Dashboard xe ô tô** - một cái nhìn tổng thể:
```
Tốc độ? ← Dashboard
Dầu? ← Dashboard  
Nhiệt độ? ← Dashboard
Áp suất lốp? ← Dashboard
```

**Không cần mở 10 cái hộp khác nhau để kiểm tra**

---

### **Grafana trong dự án giúp bạn:**

| Vấn đề | Giải pháp Grafana |
|--------|------------------|
| **Code có bug không?** | Xem Test Results panel → Nếu failed ↑ = có bug |
| **Dịch vụ hoạt động bình thường không?** | Xem Service Status → up/down |
| **API chạy nhanh không?** | Xem Response Time panel → ms |
| **Server bị quá tải không?** | Xem CPU/Memory → % |
| **Database kết nối được không?** | Xem Database panel → OK/Error |
| **Hiệu suất có giảm không?** | Xem Historical data → Compare |
| **Có vấn đề gì xảy ra giờ?** | Xem toàn bộ dashboard → ngay tức khắc |

---

### **Kết luận:**

✅ **Grafana = Giải pháp quản lý tập trung (không chỉ test)**  
✅ **Giúp bạn biết tất cả điều cần biết về hệ thống** (test, service, performance, resource)  
✅ **Tự động, real-time, không cần thủ công**  
✅ **Phát hiện vấn đề sớm trước khi user phát hiện**  

**Với Grafana: Bạn luôn nắm tay tình hình hệ thống!** 🚀
