# 🚀 Hướng dẫn chạy hệ thống FastFoodDrone

## 1️⃣ Kiểm tra yêu cầu
```powershell
# Kiểm tra Docker đã cài chưa
docker --version
docker-compose --version

# Kiểm tra Java đã cài chưa
java -version
```

## 2️⃣ Build tất cả các service (lần đầu hoặc sau khi thay đổi code)
```powershell
# Vào thư mục backend
cd d:\cnpm\CNPM-3\DoAnCNPM_Backend

# Build từng service (nếu chỉ cần build một vài service)
cd user_service && mvn clean package -DskipTests && cd ..
cd product_service && mvn clean package -DskipTests && cd ..
cd order_service && mvn clean package -DskipTests && cd ..
cd payment_service && mvn clean package -DskipTests && cd ..
cd drone_service && mvn clean package -DskipTests && cd ..
cd restaurant-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd eureka_server && mvn clean package -DskipTests && cd ..

# Hoặc build tất cả cùng lúc (nhanh hơn)
cd d:\cnpm\CNPM-3\DoAnCNPM_Backend
for /d %i in (*_service eureka_server api-gateway restaurant-service) do cd %i && mvn clean package -DskipTests && cd ..
```

## 3️⃣ Build Docker images
```powershell
cd d:\cnpm\CNPM-3

# Build từng service
docker build -t api-gateway:latest ./DoAnCNPM_Backend/api-gateway
docker build -t drone-service:latest ./DoAnCNPM_Backend/drone_service
docker build -t eureka-server:latest ./DoAnCNPM_Backend/eureka_server
docker build -t order-service:latest ./DoAnCNPM_Backend/order_service
docker build -t payment-service:latest ./DoAnCNPM_Backend/payment_service
docker build -t product-service:latest ./DoAnCNPM_Backend/product_service
docker build -t restaurant-service:latest ./DoAnCNPM_Backend/restaurant-service
docker build -t user-service:latest ./DoAnCNPM_Backend/user_service
```

## 4️⃣ Khởi động hệ thống với Docker Compose
```powershell
cd d:\cnpm\CNPM-3

# Khởi động tất cả containers (15 services)
docker-compose up -d

# Hoặc khởi động và xem logs
docker-compose up

# Kiểm tra tất cả containers đang chạy
docker ps

# Xem logs của một service cụ thể
docker logs user-service
docker logs product-service
docker logs prometheus
docker logs grafana
```

## 5️⃣ Chạy Unit Tests
```powershell
cd d:\cnpm\CNPM-3

# Chạy test một service
cd DoAnCNPM_Backend\product_service && mvn test -q && cd ..\..
cd DoAnCNPM_Backend\drone_service && mvn test -q && cd ..\..
cd DoAnCNPM_Backend\order_service && mvn test -q && cd ..\..
cd DoAnCNPM_Backend\payment_service && mvn test -q && cd ..\..

# Chạy tất cả tests (nhanh hơn)
cd d:\cnpm\CNPM-3\DoAnCNPM_Backend
for /d %i in (product_service drone_service order_service payment_service) do cd %i && mvn test -q && cd ..
```

## 6️⃣ Parse Test Metrics và Import vào Grafana
```powershell
cd d:\cnpm\CNPM-3

# Parse test results từ JUnit XML files
python .\scripts\test_metrics_parser.py

# Output sẽ là:
# - d:\cnpm\CNPM-3\monitoring\metrics\test_metrics.json
# - d:\cnpm\CNPM-3\monitoring\metrics\test_metrics.txt
```

## 7️⃣ Truy cập các services
```
📊 Grafana:          http://localhost:3001
   Username: admin
   Password: 1admin1

📈 Prometheus:       http://localhost:9090

🌍 Eureka Server:    http://localhost:8761

🔌 API Gateway:      http://localhost:8085

🛡️ User Service:     http://localhost:8081
🍕 Product Service:  http://localhost:8088
📦 Order Service:    http://localhost:8082
💳 Payment Service:  http://localhost:8084
🚁 Drone Service:    http://localhost:8089
🍽️  Restaurant Service: http://localhost:8083
```

## 8️⃣ Dừng hệ thống
```powershell
cd d:\cnpm\CNPM-3

# Dừng tất cả containers (giữ lại dữ liệu)
docker-compose stop

# Dừng và xóa containers
docker-compose down

# Dừng và xóa hết (bao gồm volumes - dữ liệu)
docker-compose down -v
```

## 📋 Quy trình đầy đủ (One-shot)
```powershell
# 1. Vào thư mục project
cd d:\cnpm\CNPM-3

# 2. Build tất cả services
cd DoAnCNPM_Backend
for /d %i in (*_service eureka_server api-gateway restaurant-service) do (
    echo Building %i...
    cd %i
    mvn clean package -DskipTests
    cd ..
)
cd ..

# 3. Build Docker images
docker build -t api-gateway:latest ./DoAnCNPM_Backend/api-gateway
docker build -t drone-service:latest ./DoAnCNPM_Backend/drone_service
docker build -t eureka-server:latest ./DoAnCNPM_Backend/eureka_server
docker build -t order-service:latest ./DoAnCNPM_Backend/order_service
docker build -t payment-service:latest ./DoAnCNPM_Backend/payment_service
docker build -t product-service:latest ./DoAnCNPM_Backend/product_service
docker build -t restaurant-service:latest ./DoAnCNPM_Backend/restaurant-service
docker build -t user-service:latest ./DoAnCNPM_Backend/user_service

# 4. Khởi động hệ thống
docker-compose up -d

# 5. Chờ 30 giây để services khởi động
Start-Sleep -Seconds 30

# 6. Chạy tests
cd DoAnCNPM_Backend
for /d %i in (product_service drone_service order_service payment_service) do (
    echo Testing %i...
    cd %i
    mvn test -q
    cd ..
)
cd ..

# 7. Parse metrics
python .\scripts\test_metrics_parser.py

# 8. Mở Grafana
Start-Process "http://localhost:3001/d/fastfood-services"
```

## 🔍 Kiểm tra trạng thái
```powershell
# Xem tất cả containers
docker ps -a

# Xem metrics được collect
curl http://localhost:9091/metrics

# Xem Prometheus targets
curl http://localhost:9090/api/v1/targets

# Xem test metrics
type d:\cnpm\CNPM-3\monitoring\metrics\test_metrics.txt
```

## 🛠️ Xử lý sự cố
```powershell
# Nếu metrics-server không chạy
docker logs metrics-server

# Nếu Prometheus không scrape được
docker logs prometheus

# Nếu Grafana không tải dashboard
docker restart grafana

# Xóa tất cả và chạy lại từ đầu
docker-compose down -v
docker system prune -a
docker-compose up -d
```

---

**Tổng thời gian:**
- Build (lần đầu): ~10-15 phút
- Build Docker images: ~5 phút
- Khởi động containers: ~1 phút
- Chạy tests: ~3 phút
- **Tổng cộng:** ~20-25 phút lần đầu, ~10 phút những lần sau
