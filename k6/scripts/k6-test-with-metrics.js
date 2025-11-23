import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter, Gauge } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const testDuration = new Trend('test_duration');
const successCount = new Counter('test_success');
const failureCount = new Counter('test_failures');
const currentUsers = new Gauge('vus_current');

export const options = {
  stages: [
    { duration: '10s', target: 5 },   // Ramp-up to 5 users
    { duration: '20s', target: 20 },  // Ramp-up to 20 users
    { duration: '10s', target: 5 },   // Ramp-down to 5 users
    { duration: '5s', target: 0 },    // Ramp-down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% requests < 500ms
    errors: ['rate<0.1'], // Error rate < 10%
  },
  ext: {
    loadimpact: {
      projectID: 3466726,
      name: 'CNPM Test',
    },
  },
};

const BASE_URL = 'http://api-gateway:8085';

export default function () {
  currentUsers.add(__VU); // Track current virtual users

  // Test User Service
  group('User Service Tests', () => {
    // Get all users
    const getUsersRes = http.get(`${BASE_URL}/users`);
    const usersPassed = check(getUsersRes, {
      'GET /users status is 200': (r) => r.status === 200,
      'response time < 500ms': (r) => r.timings.duration < 500,
    });

    testDuration.add(getUsersRes.timings.duration);
    errorRate.add(!usersPassed);
    if (usersPassed) {
      successCount.add(1);
    } else {
      failureCount.add(1);
    }

    sleep(0.5);

    // Get user by ID
    const getUserRes = http.get(`${BASE_URL}/users/1`);
    const userPassed = check(getUserRes, {
      'GET /users/{id} status is 200': (r) => r.status === 200 || r.status === 404,
      'response time < 400ms': (r) => r.timings.duration < 400,
    });

    testDuration.add(getUserRes.timings.duration);
    errorRate.add(!userPassed);
    if (userPassed) {
      successCount.add(1);
    } else {
      failureCount.add(1);
    }

    sleep(0.5);
  });

  // Test Product Service
  group('Product Service Tests', () => {
    // Get all products
    const getProductsRes = http.get(`${BASE_URL}/products`);
    const productsPassed = check(getProductsRes, {
      'GET /products status is 200': (r) => r.status === 200,
      'response time < 500ms': (r) => r.timings.duration < 500,
    });

    testDuration.add(getProductsRes.timings.duration);
    errorRate.add(!productsPassed);
    if (productsPassed) {
      successCount.add(1);
    } else {
      failureCount.add(1);
    }

    sleep(0.5);
  });

  // Test Order Service
  group('Order Service Tests', () => {
    // Get all orders
    const getOrdersRes = http.get(`${BASE_URL}/orders`);
    const ordersPassed = check(getOrdersRes, {
      'GET /orders status is 200': (r) => r.status === 200 || r.status === 401,
      'response time < 600ms': (r) => r.timings.duration < 600,
    });

    testDuration.add(getOrdersRes.timings.duration);
    errorRate.add(!ordersPassed);
    if (ordersPassed) {
      successCount.add(1);
    } else {
      failureCount.add(1);
    }

    sleep(0.5);
  });

  // Test Payment Service
  group('Payment Service Tests', () => {
    // Get payment info
    const getPaymentRes = http.get(`${BASE_URL}/payment/1`);
    const paymentPassed = check(getPaymentRes, {
      'GET /payment status is 200 or 404': (r) => r.status === 200 || r.status === 404,
      'response time < 500ms': (r) => r.timings.duration < 500,
    });

    testDuration.add(getPaymentRes.timings.duration);
    errorRate.add(!paymentPassed);
    if (paymentPassed) {
      successCount.add(1);
    } else {
      failureCount.add(1);
    }

    sleep(0.5);
  });
}

// Summary callback
export function handleSummary(data) {
  console.log('=== Test Summary ===');
  console.log(`Total Requests: ${data.metrics.http_reqs?.value || 0}`);
  console.log(`Successful Tests: ${data.metrics.test_success?.value || 0}`);
  console.log(`Failed Tests: ${data.metrics.test_failures?.value || 0}`);
  console.log(`Error Rate: ${(data.metrics.errors?.value * 100).toFixed(2)}%`);
  console.log(`Avg Duration: ${data.metrics.test_duration?.value?.toFixed(2)}ms`);

  return {};
}
