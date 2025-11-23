# 🚀 K6 Performance Testing - Quick Start Guide

## 📋 Prerequisites

✅ Docker Desktop running
✅ `docker-compose up -d` running all services
✅ Prometheus collecting metrics
✅ Grafana dashboard available

## 🎯 Running K6 Tests

### Option 1: Quick Test (20 seconds) - Recommended for first run
```powershell
cd d:\cnpm\CNPM-3
docker-compose exec k6 run /scripts/quick-k6-test.js
```

### Option 2: Demo Test with Custom Metrics
```powershell
cd d:\cnpm\CNPM-3
docker-compose exec k6 run /scripts/demo-k6-test.js
```

### Option 3: Simple Test
```powershell
cd d:\cnpm\CNPM-3
docker-compose exec k6 run /scripts/simple-k6-test.js
```

## 📊 What Each Test Does

### quick-k6-test.js (Recommended)
- **Duration**: 20 seconds
- **VUs**: 3 virtual users
- **Endpoints Tested**: 
  - ✅ API Gateway Health Check (port 8085)
  - ✅ Eureka Server (port 8761)
  - ✅ Product Service (port 8088)
  - ✅ User Service (port 8081)
  - ✅ Order Service (port 8082)
- **Metrics Collected**:
  - Request count
  - Response time (avg, p95, p99)
  - Error rate
  - Active users gauge
- **Output**: Pretty-printed summary with metrics

### demo-k6-test.js (Full-featured)
- **Duration**: 120 seconds
- **Load Profile**: 
  - 0-30s: Ramp up from 0 to 5 VUs
  - 30-90s: Stay at 10 VUs
  - 90-120s: Ramp down to 0 VUs
- **Custom Metrics**: Rate, Trend, Counter, Gauge
- **Thresholds**: P95<500ms, P99<1000ms, Error Rate<10%

### simple-k6-test.js (Minimal)
- **Duration**: 30 seconds
- **VUs**: 5 virtual users
- **Basic checks** for all endpoints

## 📈 Viewing Results

### 1. Real-time K6 Output
After running a test, K6 displays:
```
checks               ✓ 95.24%
errors               ✓ 0.00%
http_req_duration... avg=156ms p(95)=325ms p(99)=580ms
http_reqs            250
```

### 2. Prometheus Metrics
Visit: http://localhost:9090
Search for:
- `k6_http_reqs_total` - Total requests
- `k6_http_req_duration_bucket` - Response time distribution
- `k6_check_failure_rate` - Check failure rate

### 3. Grafana Dashboard (Best View)
Visit: http://localhost:3001/d/fastfood-services

Look for panels:
- 🚀 K6 Requests/sec
- ⏱️ K6 Response Time p95/p99
- ❌ K6 Error Rate
- 📊 Services Status

The dashboard updates every 5 seconds!

## 🔧 Troubleshooting

### ❌ "Cannot connect to daemon" error
Make sure Docker Desktop is running:
```powershell
docker ps
```

### ❌ "No such container" error
Make sure services are running:
```powershell
cd d:\cnpm\CNPM-3
docker-compose up -d
docker-compose ps
```

### ❌ Connection refused errors
Give services time to start (wait 30-60 seconds):
```powershell
docker-compose logs api-gateway
```

### ❌ K6 metrics not showing in Grafana
Check K6 exporter is running:
```powershell
docker-compose logs k6-exporter
curl http://localhost:6565
```

## 📝 Custom K6 Scripts

To create your own K6 test, save a `.js` file in `d:\cnpm\CNPM-3\scripts\`:

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
};

export default function () {
  const res = http.get('http://api-gateway:8085/actuator/health');
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
```

Then run:
```powershell
docker-compose exec k6 run /scripts/your-test.js
```

## 🎬 Recommended Workflow

1. **Start Services**:
   ```powershell
   cd d:\cnpm\CNPM-3
   docker-compose up -d
   ```

2. **Wait for everything to be ready** (30 seconds)

3. **Open Grafana Dashboard**:
   - Browser: http://localhost:3001/d/fastfood-services
   - Arrange window to see dashboard

4. **Run Quick Test**:
   ```powershell
   docker-compose exec k6 run /scripts/quick-k6-test.js
   ```

5. **Watch Dashboard Update**:
   - K6 panels will show real-time data
   - Metrics appear within 5 seconds
   - Runs for 20 seconds total

6. **View Results** in console output and Grafana

## 💡 Pro Tips

- Run quick-k6-test.js first to verify setup
- Use demo-k6-test.js for sustained load testing
- Monitor Prometheus targets: http://localhost:9090/targets
- Check individual service logs if tests fail:
  ```powershell
  docker-compose logs service-name
  ```
- K6 exporter pushes metrics every 5 seconds (Prometheus scrape interval)

## 📚 More Information

- K6 Docs: https://k6.io/docs/
- Prometheus Docs: https://prometheus.io/docs/
- Grafana Docs: https://grafana.com/docs/

Happy Load Testing! 🚀
