import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

export default function () {
  // Test Health Check
  const res1 = http.get('http://api-gateway:8085/actuator/health');
  check(res1, {
    '✅ Health Check 200': (r) => r.status === 200,
  });
  
  sleep(0.5);
  
  // Test Eureka
  const res2 = http.get('http://eureka-server:8761');
  check(res2, {
    '✅ Eureka 200': (r) => r.status === 200,
  });
  
  sleep(0.5);
  
  // Test Product Service
  const res3 = http.get('http://product-service:8088/api/products');
  check(res3, {
    '✅ Product Service 200/404': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.5);
  
  // Test User Service
  const res4 = http.get('http://user-service:8081/api/users');
  check(res4, {
    '✅ User Service 200/404': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.5);
  
  // Test Order Service
  const res5 = http.get('http://order-service:8082/api/orders');
  check(res5, {
    '✅ Order Service 200/404': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.5);
}
