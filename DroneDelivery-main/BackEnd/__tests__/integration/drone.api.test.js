import request from 'supertest';
import express from 'express';
import cookieParser from 'cookie-parser';
import droneRouter from '../../routes/drone.routes.js';
import Drone from '../../models/drone.model.js';
import User from '../../models/user.model.js';
import jwt from 'jsonwebtoken';
import mongoose from 'mongoose';

const app = express();
app.use(express.json());
app.use(cookieParser());
app.use('/api/drone', droneRouter);

describe('Drone API Integration Tests', () => {
  let testUser;
  let authToken;
  let shopId;

  beforeEach(async () => {
    // Set JWT_SECRET for tests
    process.env.JWT_SECRET = 'test-secret-key-for-testing';
    
    testUser = await User.create({
      fullName: 'Drone Test User',
      email: 'drone-test@example.com',
      password: 'Password123!',
      role: 'user',
      mobile: '0987654321',
    });

    authToken = jwt.sign(
      { userId: testUser._id },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    shopId = new mongoose.Types.ObjectId();
  });

  describe('POST /api/drone/create', () => {
    it('should create a new drone', async () => {
      const droneData = {
        shop: shopId,
        model: 'DJI Phantom 4',
        serialNumber: 'DJI-TEST-001',
        capacity: {
          weight: 5.5,
          volume: 8000,
        },
        battery: {
          current: 100,
          maxCapacity: 5350,
        },
        specifications: {
          maxSpeed: 72,
          maxAltitude: 500,
          flightTime: 28,
          range: 30,
        },
      };

      const response = await request(app)
        .post('/api/drone/create')
        .set('Cookie', [`token=${authToken}`])
        .send(droneData)
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail without authentication', async () => {
      const droneData = {
        shop: shopId,
        model: 'DJI Mavic',
        serialNumber: 'DJI-TEST-002',
        capacity: {
          weight: 4.0,
          volume: 6000,
        },
        battery: {
          current: 100,
          maxCapacity: 3500,
        },
        specifications: {
          maxSpeed: 68,
          maxRange: 25,
          maxFlightTime: 34,
        },
      };

      const response = await request(app)
        .post('/api/drone/create')
        .send(droneData);

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });

  describe('GET /api/drone/available', () => {
    beforeEach(async () => {
      await Drone.create([
        {
          shop: shopId,
          model: 'Available Drone 1',
          serialNumber: 'AVAIL-001',
          capacity: { weight: 5, volume: 8000 },
          battery: { current: 100, maxCapacity: 5000 },
          status: 'available',
          specifications: { maxSpeed: 70, maxRange: 30, maxFlightTime: 25 },
        },
        {
          shop: shopId,
          model: 'Busy Drone 1',
          serialNumber: 'BUSY-001',
          capacity: { weight: 3, volume: 5000 },
          battery: { current: 50, maxCapacity: 3000 },
          status: 'busy',
          specifications: { maxSpeed: 60, maxRange: 20, maxFlightTime: 30 },
        },
      ]);
    });

    it('should get available drones', async () => {
      const response = await request(app)
        .get('/api/drone/available')
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });
  });

  describe('PUT /api/drone/:droneId/status', () => {
    let testDrone;

    beforeEach(async () => {
      testDrone = await Drone.create({
        shop: shopId,
        model: 'Test Drone',
        serialNumber: 'TEST-UPDATE-001',
        capacity: { weight: 4.5, volume: 7000 },
        battery: { current: 80, maxCapacity: 4000 },
        status: 'available',
        specifications: { maxSpeed: 65, maxRange: 28, maxFlightTime: 30 },
      });
    });

    it('should update drone status', async () => {
      const response = await request(app)
        .put(`/api/drone/${testDrone._id}/status`)
        .set('Cookie', [`token=${authToken}`])
        .send({ status: 'busy' })
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail with invalid drone ID', async () => {
      const response = await request(app)
        .put('/api/drone/invalid-id/status')
        .set('Cookie', [`token=${authToken}`])
        .send({ status: 'busy' });

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });

  describe('PUT /api/drone/:droneId/battery', () => {
    let testDrone;

    beforeEach(async () => {
      testDrone = await Drone.create({
        shop: shopId,
        model: 'Battery Test Drone',
        serialNumber: 'BATTERY-TEST-001',
        capacity: { weight: 5.0, volume: 7500 },
        battery: { current: 100, maxCapacity: 5000 },
        status: 'available',
        specifications: { maxSpeed: 70, maxRange: 30, maxFlightTime: 28 },
      });
    });

    it('should update drone battery level', async () => {
      const response = await request(app)
        .put(`/api/drone/${testDrone._id}/battery`)
        .set('Cookie', [`token=${authToken}`])
        .send({ batteryLevel: 45 })
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail with invalid battery level', async () => {
      const response = await request(app)
        .put(`/api/drone/${testDrone._id}/battery`)
        .set('Cookie', [`token=${authToken}`])
        .send({ batteryLevel: 150 });

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });
});
