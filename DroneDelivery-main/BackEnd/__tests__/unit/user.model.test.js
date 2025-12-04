import User from '../../models/user.model.js';
import bcrypt from 'bcryptjs';

describe('User Model Unit Tests', () => {
  describe('User Creation', () => {
    it('should create a new user with valid data', async () => {
      const userData = {
        fullName: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
        mobile: '0123456789',
        role: 'user',
      };

      const user = new User(userData);
      const savedUser = await user.save();

      expect(savedUser._id).toBeDefined();
      expect(savedUser.fullName).toBe(userData.fullName);
      expect(savedUser.email).toBe(userData.email);
      expect(savedUser.mobile).toBe(userData.mobile);
    });

    it('should fail to create user without required fields', async () => {
      const user = new User({
        fullName: 'John Doe',
      });

      let error;
      try {
        await user.save();
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
      expect(error.name).toBe('ValidationError');
    });

    it('should not allow duplicate email', async () => {
      const userData = {
        fullName: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
        mobile: '0123456789',
        role: 'user',
      };

      await User.create(userData);

      let error;
      try {
        await User.create(userData);
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
      expect(error.code).toBe(11000); // MongoDB duplicate key error
    });
  });

  describe('User Password Hashing', () => {
    it('should hash password before saving', async () => {
      const plainPassword = 'password123';
      const user = new User({
        fullName: 'John Doe',
        email: 'john@example.com',
        password: plainPassword,
        mobile: '0123456789',
        role: 'user',
      });

      await user.save();

      expect(user.password).not.toBe(plainPassword);
      expect(user.password.length).toBeGreaterThan(plainPassword.length);
    });

    it('should validate correct password', async () => {
      const plainPassword = 'password123';
      const user = new User({
        fullName: 'John Doe',
        email: 'john@example.com',
        password: plainPassword,
        mobile: '0123456789',
        role: 'user',
      });

      await user.save();
      const isMatch = await bcrypt.compare(plainPassword, user.password);

      expect(isMatch).toBe(true);
    });

    it('should reject incorrect password', async () => {
      const user = new User({
        fullName: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
        mobile: '0123456789',
        role: 'user',
      });

      await user.save();
      const isMatch = await bcrypt.compare('wrongpassword', user.password);

      expect(isMatch).toBe(false);
    });
  });

  describe('User Query Methods', () => {
    beforeEach(async () => {
      await User.create([
        {
          fullName: 'User 1',
          email: 'user1@example.com',
          password: 'password123',
          mobile: '0123456781',
          role: 'user',
        },
        {
          fullName: 'User 2',
          email: 'user2@example.com',
          password: 'password123',
          mobile: '0123456782',
          role: 'user',
        },
      ]);
    });

    it('should find user by email', async () => {
      const user = await User.findOne({ email: 'user1@example.com' });

      expect(user).toBeDefined();
      expect(user.fullName).toBe('User 1');
    });

    it('should find all users', async () => {
      const users = await User.find();

      expect(users).toHaveLength(2);
    });

    it('should update user information', async () => {
      const user = await User.findOne({ email: 'user1@example.com' });
      user.fullName = 'Updated Name';
      await user.save();

      const updatedUser = await User.findById(user._id);
      expect(updatedUser.fullName).toBe('Updated Name');
    });

    it('should delete user', async () => {
      const user = await User.findOne({ email: 'user1@example.com' });
      await User.findByIdAndDelete(user._id);

      const deletedUser = await User.findById(user._id);
      expect(deletedUser).toBeNull();
    });
  });
});
