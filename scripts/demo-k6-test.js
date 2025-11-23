import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter, Gauge } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const requestDuration = new Trend('request_duration');
const requestCounter = new Counter('requests');
const activeUsers = new Gauge('active_users');

export const options = {
  stages: [
    { duration: '30s', target: 5 },   // Ramp-up to 5 users
    { duration: '1m', target: 10 },   // Stay at 10 users
    { duration: '30s', target: 0 },   // Ramp-down to 0 users
  ],
  thresholds: {
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],
    'errors': ['rate<0.1'],
  },
};

export default function () {
  activeUsers.add(1);
  
  // Test 1: Health Check
  const healthCheck = http.get('http://localhost:8085/actuator/health');
  check(healthCheck, {
    'health check status is 200': (r) => r.status === 200,
  }) || errorRate.add(1);
  
  requestDuration.add(healthCheck.timings.duration);
  requestCounter.add(1);
  
  sleep(1);
  
  // Test 2: Eureka
  const eurekaCheck = http.get('http://localhost:8761');
  check(eurekaCheck, {
    'eureka status is 200': (r) => r.status === 200,
  }) || errorRate.add(1);
  
  requestDuration.add(eurekaCheck.timings.duration);
  requestCounter.add(1);
  
  sleep(1);
  
  // Test 3: Product Service
  const productTest = http.get('http://localhost:8088/api/products');
  check(productTest, {
    'product service status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(productTest.timings.duration);
  requestCounter.add(1);
  
  sleep(1);
  
  // Test 4: User Service
  const userTest = http.get('http://localhost:8081/api/users');
  check(userTest, {
    'user service status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(userTest.timings.duration);
  requestCounter.add(1);
  
  sleep(1);
  
  // Test 5: Order Service
  const orderTest = http.get('http://localhost:8082/api/orders');
  check(orderTest, {
    'order service status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(orderTest.timings.duration);
  requestCounter.add(1);
  
  sleep(1);
  
  activeUsers.add(-1);
}

export function handleSummary(data) {
  console.log('📊 ============ K6 TEST SUMMARY ============');
  console.log(`Total Requests: ${data.metrics.requests.value}`);
  console.log(`Error Rate: ${(data.metrics.errors.value * 100).toFixed(2)}%`);
  console.log(`Avg Duration: ${data.metrics.request_duration.values.avg.toFixed(2)}ms`);
  console.log(`Max Duration: ${data.metrics.request_duration.values.max.toFixed(2)}ms`);
  console.log(`P95 Duration: ${data.metrics.request_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`P99 Duration: ${data.metrics.request_duration.values['p(99)'].toFixed(2)}ms`);
  console.log('=========================================');
  
  return {
    'stdout': textSummary(data),
  };
}

function textSummary(data) {
  let summary = '\n✅ K6 Test Results:\n';
  summary += `├─ Requests: ${data.metrics.requests.value}\n`;
  summary += `├─ Errors: ${(data.metrics.errors.value * 100).toFixed(2)}%\n`;
  summary += `├─ Avg Response: ${data.metrics.request_duration.values.avg.toFixed(2)}ms\n`;
  summary += `├─ P95 Response: ${data.metrics.request_duration.values['p(95)'].toFixed(2)}ms\n`;
  summary += `└─ P99 Response: ${data.metrics.request_duration.values['p(99)'].toFixed(2)}ms\n`;
  return summary;
}
