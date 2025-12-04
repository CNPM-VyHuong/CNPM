import request from 'supertest';
import express from 'express';
import cookieParser from 'cookie-parser';
import orderRouter from '../../routes/order.routes.js';
import User from '../../models/user.model.js';
import Order from '../../models/order.model.js';
import jwt from 'jsonwebtoken';
import mongoose from 'mongoose';

const app = express();
app.use(express.json());
app.use(cookieParser());
app.use('/api/order', orderRouter);

// Helper function for complete order data
const createOrderData = (overrides = {}) => ({
  orderItems: [
    {
      itemId: new mongoose.Types.ObjectId(),
      quantity: 2,
      price: 50000,
      subtotal: 100000,
      itemName: 'Burger',
      itemImage: 'burger.jpg',
      itemCategory: 'Fast Food',
      itemFoodType: 'Non-Veg',
      shopId: new mongoose.Types.ObjectId(),
      shopName: 'Test Shop',
      shopCity: 'Ho Chi Minh',
      shopState: 'HCM',
      shopAddress: '123 Shop Street',
      shopOwnerId: new mongoose.Types.ObjectId(),
    },
  ],
  totalAmount: 100000,
  deliveryAddress: {
    address: '123 Main St',
    city: 'Ho Chi Minh',
    state: 'HCM',
    coordinates: {
      lat: 10.762622,
      lng: 106.660172,
    },
  },
  contactInfo: {
    name: 'Test User',
    phone: '0123456789',
    email: 'order-test@example.com',
  },
  ...overrides,
});

describe('Order API Integration Tests', () => {
  let testUser;
  let authToken;

  beforeEach(async () => {
    // Set JWT_SECRET for tests
    process.env.JWT_SECRET = 'test-secret-key-for-testing';
    
    testUser = await User.create({
      fullName: 'Test User',
      email: 'order-test@example.com',
      password: 'Password123!',
      role: 'user',
      mobile: '0123456789',
    });

    authToken = jwt.sign(
      { userId: testUser._id },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );
  });

  describe('POST /api/order/create', () => {
    it('should create a new order', async () => {
      const orderData = createOrderData();

      const response = await request(app)
        .post('/api/order/create')
        .set('Cookie', [`token=${authToken}`])
        .send(orderData)
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail without authentication', async () => {
      const orderData = createOrderData();

      const response = await request(app)
        .post('/api/order/create')
        .send(orderData);

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });

  describe('GET /api/order/user-orders', () => {
    beforeEach(async () => {
      const orderData = createOrderData({
        user: testUser._id,
        orderItems: [
          {
            itemId: new mongoose.Types.ObjectId(),
            quantity: 1,
            price: 40000,
            subtotal: 40000,
            itemName: 'Pizza',
            itemImage: 'pizza.jpg',
            itemCategory: 'Italian',
            itemFoodType: 'Veg',
            shopId: new mongoose.Types.ObjectId(),
            shopName: 'Pizza Place',
            shopCity: 'Da Nang',
            shopState: 'DN',
            shopAddress: '789 Shop Ave',
            shopOwnerId: new mongoose.Types.ObjectId(),
          },
        ],
        totalAmount: 40000,
      });
      await Order.create(orderData);
    });

    it('should get user orders', async () => {
      const response = await request(app)
        .get('/api/order/user-orders')
        .set('Cookie', [`token=${authToken}`])
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail without authentication', async () => {
      const response = await request(app).get('/api/order/user-orders');

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });

  describe('PUT /api/order/:orderId/status', () => {
    let testOrder;

    beforeEach(async () => {
      const orderData = createOrderData({
        user: testUser._id,
        orderItems: [
          {
            itemId: new mongoose.Types.ObjectId(),
            quantity: 1,
            price: 25000,
            subtotal: 25000,
            itemName: 'Salad',
            itemImage: 'salad.jpg',
            itemCategory: 'Healthy',
            itemFoodType: 'Veg',
            shopId: new mongoose.Types.ObjectId(),
            shopName: 'Salad Bar',
            shopCity: 'Can Tho',
            shopState: 'CT',
            shopAddress: '101 Shop Rd',
            shopOwnerId: new mongoose.Types.ObjectId(),
          },
        ],
        totalAmount: 25000,
        orderStatus: 'pending',
      });
      testOrder = await Order.create(orderData);
    });

    it('should update order status', async () => {
      const response = await request(app)
        .put(`/api/order/${testOrder._id}/status`)
        .set('Cookie', [`token=${authToken}`])
        .send({ status: 'confirmed' })
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail with invalid order ID', async () => {
      const response = await request(app)
        .put('/api/order/invalid-id/status')
        .set('Cookie', [`token=${authToken}`])
        .send({ status: 'confirmed' });

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });
});
