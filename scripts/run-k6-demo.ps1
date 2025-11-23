# K6 Demo Test Runner
# Script chạy K6 test và hiển thị kết quả

Write-Host "🚀 Starting K6 Performance Test..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

# Setup test scenarios
$scenarios = @(
    @{ name = "Light Load"; vus = 5; duration = "30s" },
    @{ name = "Medium Load"; vus = 10; duration = "1m" },
    @{ name = "Heavy Load"; vus = 15; duration = "30s" }
)

foreach ($scenario in $scenarios) {
    Write-Host "`n📊 Running: $($scenario.name)" -ForegroundColor Yellow
    Write-Host "   VUs: $($scenario.vus), Duration: $($scenario.duration)" -ForegroundColor Gray
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    
    $cmd = @"
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: $($scenario.vus),
  duration: '$($scenario.duration)',
  thresholds: {
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],
  },
};

export default function () {
  // Test 1: Health Check
  const r1 = http.get('http://api-gateway:8085/actuator/health');
  check(r1, {
    '✅ Health 200': (r) => r.status === 200,
    '⏱️ Health <500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(0.3);
  
  // Test 2: Eureka
  const r2 = http.get('http://eureka-server:8761');
  check(r2, {
    '✅ Eureka 200': (r) => r.status === 200,
  });
  
  sleep(0.3);
  
  // Test 3: Products
  const r3 = http.get('http://product-service:8088/api/products');
  check(r3, {
    '✅ Products 200/404': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.3);
  
  // Test 4: Users
  const r4 = http.get('http://user-service:8081/api/users');
  check(r4, {
    '✅ Users 200/404': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.3);
  
  // Test 5: Orders
  const r5 = http.get('http://order-service:8082/api/orders');
  check(r5, {
    '✅ Orders 200/404': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.3);
}
"@

    # Save temp script
    $tempScript = "d:\cnpm\CNPM-3\scripts\temp-k6-test.js"
    $cmd | Out-File -FilePath $tempScript -Encoding UTF8
    
    # Run K6 via Docker
    Write-Host "🔄 Executing test..." -ForegroundColor Gray
    docker-compose -f "d:\cnpm\CNPM-3\docker-compose.yml" run --rm k6 run /scripts/temp-k6-test.js 2>&1 | Tee-Object -Variable testOutput | Out-Null
    
    # Parse and display results
    Write-Host ""
    Write-Host "📈 Results:" -ForegroundColor Green
    
    if ($testOutput -match "checks\s+(\d+\.\d+)%") {
        $checkPass = [regex]::Matches($testOutput, "checks\s+(\d+\.\d+)%") | ForEach-Object { $_.Groups[1].Value }
        Write-Host "   ✅ Check Pass Rate: $checkPass%" -ForegroundColor Green
    }
    
    if ($testOutput -match "http_req_duration\s+p\(95\)=(\d+)") {
        $p95 = [regex]::Matches($testOutput, "http_req_duration\s+p\(95\)=(\d+)") | ForEach-Object { $_.Groups[1].Value }
        Write-Host "   ⏱️ P95 Response: ${p95}ms" -ForegroundColor Cyan
    }
    
    if ($testOutput -match "http_reqs\s+(\d+)") {
        $requests = [regex]::Matches($testOutput, "http_reqs\s+(\d+)") | ForEach-Object { $_.Groups[1].Value }
        Write-Host "   📊 Total Requests: $requests" -ForegroundColor Cyan
    }
    
    Write-Host ""
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "✅ All tests completed!" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""
Write-Host "📊 Dashboard: http://localhost:3001/d/fastfood-services" -ForegroundColor Cyan
Write-Host "📈 Prometheus: http://localhost:9090" -ForegroundColor Cyan
