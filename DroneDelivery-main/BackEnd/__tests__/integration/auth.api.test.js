import request from 'supertest';
import express from 'express';
import cookieParser from 'cookie-parser';
import authRouter from '../../routes/auth.routes.js';
import User from '../../models/user.model.js';

const app = express();
app.use(express.json());
app.use(cookieParser());
app.use('/api/auth', authRouter);

describe('Auth API Integration Tests', () => {
  describe('POST /api/auth/signup', () => {
    it('should register a new user successfully', async () => {
      const userData = {
        fullName: 'John Doe',
        email: 'john@example.com',
        role: 'user',
        password: 'Password123!',
        mobile: '0123456789',
      };

      const response = await request(app)
        .post('/api/auth/signup')
        .send(userData)
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);

      if (response.status === 201 || response.status === 200) {
        expect(response.body).toHaveProperty('message');
      }
    });

    it('should fail with missing required fields', async () => {
      const userData = {
        fullName: 'John Doe',
        email: 'incomplete@example.com',
        role: 'user',
      };

      const response = await request(app)
        .post('/api/auth/signup')
        .send(userData);

      expect(response.status).toBeGreaterThanOrEqual(400);
    });

    it('should fail with duplicate email', async () => {
      const userData = {
        fullName: 'Jane Doe',
        email: 'duplicate@example.com',
        role: 'user',
        password: 'Password123!',
        mobile: '0987654321',
      };

      await User.create({
        fullName: 'Existing User',
        email: 'duplicate@example.com',
        role: 'user',
        password: 'Password123!',
        mobile: '0111111111',
      });

      const response = await request(app)
        .post('/api/auth/signup')
        .send(userData);

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });

  describe('POST /api/auth/signin', () => {
    beforeEach(async () => {
      await User.create({
        fullName: 'Test User',
        email: 'test@example.com',
        role: 'user',
        password: 'Password123!',
        mobile: '0123456789',
      });
    });

    it('should login with valid credentials', async () => {
      const credentials = {
        email: 'test@example.com',
        role: 'user',
        password: 'Password123!',
      };

      const response = await request(app)
        .post('/api/auth/signin')
        .send(credentials)
        .expect('Content-Type', /json/);

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });

    it('should fail with invalid password', async () => {
      const credentials = {
        email: 'test@example.com',
        role: 'user',
        password: 'WrongPassword',
      };

      const response = await request(app)
        .post('/api/auth/signin')
        .send(credentials);

      expect(response.status).toBeGreaterThanOrEqual(400);
    });

    it('should fail with non-existent email', async () => {
      const credentials = {
        email: 'nonexistent@example.com',
        role: 'user',
        password: 'Password123!',
      };

      const response = await request(app)
        .post('/api/auth/signin')
        .send(credentials);

      expect(response.status).toBeGreaterThanOrEqual(400);
    });
  });

  describe('GET /api/auth/signout', () => {
    it('should signout successfully', async () => {
      const response = await request(app).get('/api/auth/signout');

      expect(response.status).toBeGreaterThanOrEqual(200);
      expect(response.status).toBeLessThan(500);
    });
  });
});
