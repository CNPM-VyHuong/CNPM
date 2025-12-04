import Order from '../../models/order.model.js';
import User from '../../models/user.model.js';
import mongoose from 'mongoose';

// Helper function to create valid order data
const createValidOrderData = (user, overrides = {}) => {
  const defaultData = {
    user: user._id,
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
      email: 'test@example.com',
    },
    orderStatus: 'pending',
  };

  return { ...defaultData, ...overrides };
};

describe('Order Model Unit Tests', () => {
  let testUser;

  beforeEach(async () => {
    testUser = await User.create({
      fullName: 'Test User',
      email: 'test@example.com',
      password: 'password123',
      mobile: '0123456789',
      role: 'user',
    });
  });

  describe('Order Creation', () => {
    it('should create a new order with valid data', async () => {
      const orderData = createValidOrderData(testUser);

      const order = await Order.create(orderData);

      expect(order._id).toBeDefined();
      expect(order.user.toString()).toBe(testUser._id.toString());
      expect(order.orderItems).toHaveLength(1);
      expect(order.totalAmount).toBe(100000);
      expect(order.status).toBe('pending');
    });

    it('should fail without required fields', async () => {
      const order = new Order({
        user: testUser._id,
      });

      let error;
      try {
        await order.save();
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
      expect(error.name).toBe('ValidationError');
    });

    it('should calculate subtotal correctly', async () => {
      const orderData = createValidOrderData(testUser, {
        orderItems: [
          {
            itemId: new mongoose.Types.ObjectId(),
            quantity: 3,
            price: 25000,
            subtotal: 75000,
            itemName: 'Pizza',
            itemImage: 'pizza.jpg',
            itemCategory: 'Italian',
            itemFoodType: 'Veg',
            shopId: new mongoose.Types.ObjectId(),
            shopName: 'Pizza Shop',
            shopCity: 'Ha Noi',
            shopState: 'HN',
            shopAddress: '456 Shop Street',
            shopOwnerId: new mongoose.Types.ObjectId(),
          },
        ],
        totalAmount: 75000,
      });

      const order = await Order.create(orderData);

      expect(order.orderItems[0].subtotal).toBe(75000);
    });
  });

  describe('Order Status Updates', () => {
    it('should update order status', async () => {
      const order = await Order.create(createValidOrderData(testUser, {
        orderItems: [
          {
            itemId: new mongoose.Types.ObjectId(),
            quantity: 1,
            price: 30000,
            subtotal: 30000,
            itemName: 'Salad',
            itemImage: 'salad.jpg',
            itemCategory: 'Healthy',
            itemFoodType: 'Veg',
            shopId: new mongoose.Types.ObjectId(),
            shopName: 'Salad Bar',
            shopCity: 'Da Nang',
            shopState: 'DN',
            shopAddress: '789 Shop Avenue',
            shopOwnerId: new mongoose.Types.ObjectId(),
          },
        ],
        totalAmount: 30000,
      }));

      order.orderStatus = 'confirmed';
      await order.save();

      const updatedOrder = await Order.findById(order._id);
      expect(updatedOrder.orderStatus).toBe('confirmed');
    });

    it('should only allow valid status values', async () => {
      const orderData = createValidOrderData(testUser, {
        orderStatus: 'invalid_status',
      });
      const order = new Order(orderData);

      let error;
      try {
        await order.save();
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
    });
  });

  describe('Order Queries', () => {
    beforeEach(async () => {
      await Order.create([
        createValidOrderData(testUser, {
          orderItems: [
            {
              itemId: new mongoose.Types.ObjectId(),
              quantity: 1,
              price: 40000,
              subtotal: 40000,
              itemName: 'Sandwich',
              itemImage: 'sandwich.jpg',
              itemCategory: 'Fast Food',
              itemFoodType: 'Non-Veg',
              shopId: new mongoose.Types.ObjectId(),
              shopName: 'Sandwich Shop',
              shopCity: 'HCM',
              shopState: 'HCM',
              shopAddress: '111 Shop St',
              shopOwnerId: new mongoose.Types.ObjectId(),
            },
          ],
          totalAmount: 40000,
          orderStatus: 'pending',
        }),
        createValidOrderData(testUser, {
          orderItems: [
            {
              itemId: new mongoose.Types.ObjectId(),
              quantity: 2,
              price: 35000,
              subtotal: 70000,
              itemName: 'Noodles',
              itemImage: 'noodles.jpg',
              itemCategory: 'Asian',
              itemFoodType: 'Veg',
              shopId: new mongoose.Types.ObjectId(),
              shopName: 'Noodle House',
              shopCity: 'HCM',
              shopState: 'HCM',
              shopAddress: '222 Shop Rd',
              shopOwnerId: new mongoose.Types.ObjectId(),
            },
          ],
          totalAmount: 70000,
          orderStatus: 'delivering',
        }),
      ]);
    });

    it('should find orders by user', async () => {
      const orders = await Order.find({ user: testUser._id });

      expect(orders).toHaveLength(2);
    });

    it('should find orders by status', async () => {
      const pendingOrders = await Order.find({ orderStatus: 'pending' });

      expect(pendingOrders).toHaveLength(1);
      expect(pendingOrders[0].orderStatus).toBe('pending');
    });
  });
});
