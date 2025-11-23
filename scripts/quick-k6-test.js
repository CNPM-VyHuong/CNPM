import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter, Gauge } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const requestDuration = new Trend('request_duration');
const requestCounter = new Counter('requests_total');
const activeUsers = new Gauge('active_users');

export const options = {
  vus: 3,
  duration: '20s',
  thresholds: {
    'errors': ['rate<0.5'],  // Allow up to 50% errors during startup
    'request_duration': ['p(95)<5000'],  // P95 less than 5 seconds
  },
};

export default function () {
  activeUsers.add(1);
  
  group('API Gateway Health', () => {
    const res = http.get('http://api-gateway:8085/actuator/health', {
      tags: { name: 'HealthCheck' },
      timeout: '5s',
    });
    
    requestDuration.add(res.timings.duration);
    requestCounter.add(1);
    
    const passed = check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    errorRate.add(!passed);
  });
  
  sleep(0.3);
  
  group('Eureka Registry', () => {
    const res = http.get('http://eureka-server:8761/', {
      tags: { name: 'Eureka' },
      timeout: '5s',
    });
    
    requestDuration.add(res.timings.duration);
    requestCounter.add(1);
    
    const passed = check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    errorRate.add(!passed);
  });
  
  sleep(0.3);
  
  group('User Service Health', () => {
    const res = http.get('http://user-service:8081/actuator/health', {
      tags: { name: 'UserHealth' },
      timeout: '5s',
    });
    
    requestDuration.add(res.timings.duration);
    requestCounter.add(1);
    
    const passed = check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    errorRate.add(!passed);
  });
  
  sleep(0.3);
  
  group('Product Service Health', () => {
    const res = http.get('http://product-service:8088/actuator/health', {
      tags: { name: 'ProductHealth' },
      timeout: '5s',
    });
    
    requestDuration.add(res.timings.duration);
    requestCounter.add(1);
    
    const passed = check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    errorRate.add(!passed);
  });
  
  sleep(0.3);
  
  group('Order Service Health', () => {
    const res = http.get('http://order-service:8082/actuator/health', {
      tags: { name: 'OrderHealth' },
      timeout: '5s',
    });
    
    requestDuration.add(res.timings.duration);
    requestCounter.add(1);
    
    const passed = check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    errorRate.add(!passed);
  });
  
  activeUsers.add(-1);
  sleep(0.5);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
  };
}

function textSummary(data, options) {
  const opts = options || {};
  const indent = opts.indent || '';
  const enableColors = opts.enableColors !== false;
  
  const color = enableColors ? {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    cyan: '\x1b[36m',
    reset: '\x1b[0m',
  } : {
    green: '',
    red: '',
    yellow: '',
    cyan: '',
    reset: '',
  };
  
  const metrics = data.metrics;
  let output = '\n' + color.cyan + '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ K6 TEST SUMMARY ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' + color.reset + '\n';
  
  // Requests
  if (metrics.http_reqs) {
    const reqs = metrics.http_reqs.values.count || 0;
    const rate = metrics.http_reqs.values.rate || 0;
    output += `${indent}${color.green}✅ Total Requests: ${reqs}${color.reset}\n`;
    output += `${indent}${color.green}📊 Request Rate: ${rate.toFixed(2)} req/s${color.reset}\n`;
  }
  
  // Duration
  if (metrics.http_req_duration) {
    const p95 = metrics.http_req_duration.values['p(95)'] || 0;
    const p99 = metrics.http_req_duration.values['p(99)'] || 0;
    const avg = metrics.http_req_duration.values.avg || 0;
    output += `${indent}${color.cyan}⏱️  Avg Response: ${avg.toFixed(0)}ms${color.reset}\n`;
    output += `${indent}${color.cyan}⏱️  P95 Response: ${p95.toFixed(0)}ms${color.reset}\n`;
    output += `${indent}${color.cyan}⏱️  P99 Response: ${p99.toFixed(0)}ms${color.reset}\n`;
  }
  
  // Errors
  if (metrics.errors && metrics.errors.values.rate !== undefined) {
    const errorRate = metrics.errors.values.rate * 100;
    const errorColor = errorRate > 5 ? color.red : color.green;
    output += `${indent}${errorColor}❌ Error Rate: ${errorRate.toFixed(2)}%${color.reset}\n`;
  }
  
  output += '\n' + color.cyan + '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' + color.reset + '\n';
  output += `\n📈 Open Grafana: ${color.green}http://localhost:3001${color.reset}\n\n`;
  
  return output;
}
