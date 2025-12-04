import Drone from '../../models/drone.model.js';
import mongoose from 'mongoose';

describe('Drone Model Unit Tests', () => {
  const shopId = new mongoose.Types.ObjectId();

  describe('Drone Creation', () => {
    it('should create a new drone with valid data', async () => {
      const droneData = {
        shop: shopId,
        model: 'DJI Phantom 4',
        serialNumber: 'DJI-PH4-001',
        capacity: {
          weight: 5.5,
          volume: 8000,
        },
        battery: {
          current: 100,
          maxCapacity: 5350,
        },
        status: 'available',
        specifications: {
          maxSpeed: 72,
          maxAltitude: 120,
          maxAltitude: 120,
          range: 30,
          flightTime: 28,
        },
      };

      const drone = await Drone.create(droneData);

      expect(drone._id).toBeDefined();
      expect(drone.model).toBe('DJI Phantom 4');
      expect(drone.serialNumber).toBe('DJI-PH4-001');
      expect(drone.status).toBe('available');
      expect(drone.battery.current).toBe(100);
    });

    it('should fail without required fields', async () => {
      const drone = new Drone({
        model: 'DJI Mavic',
      });

      let error;
      try {
        await drone.save();
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
      expect(error.name).toBe('ValidationError');
    });

    it('should not allow duplicate serial numbers', async () => {
      const droneData = {
        shop: shopId,
        model: 'DJI Phantom 4',
        serialNumber: 'DJI-PH4-UNIQUE',
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
          maxAltitude: 120,
          maxAltitude: 120,
          range: 30,
          flightTime: 28,
        },
      };

      await Drone.create(droneData);

      let error;
      try {
        await Drone.create(droneData);
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
      expect(error.code).toBe(11000);
    });
  });

  describe('Drone Status Management', () => {
    it('should update drone status', async () => {
      const drone = await Drone.create({
        shop: shopId,
        model: 'DJI Mavic Air 2',
        serialNumber: 'DJI-MA2-001',
        capacity: {
          weight: 4.0,
          volume: 6000,
        },
        battery: {
          current: 85,
          maxCapacity: 3500,
        },
        status: 'available',
        specifications: {
          maxSpeed: 68,
          maxAltitude: 120,
          maxAltitude: 120,
          range: 25,
          flightTime: 34,
        },
      });

      drone.status = 'busy';
      await drone.save();

      const updatedDrone = await Drone.findById(drone._id);
      expect(updatedDrone.status).toBe('busy');
    });

    it('should only allow valid status values', async () => {
      const drone = new Drone({
        shop: shopId,
        model: 'DJI Mini 3',
        serialNumber: 'DJI-MINI3-001',
        capacity: {
          weight: 2.5,
          volume: 4000,
        },
        battery: {
          current: 100,
          maxCapacity: 2453,
        },
        status: 'invalid_status',
        specifications: {
          maxSpeed: 57,
          maxAltitude: 120,
          range: 20,
          flightTime: 38,
        },
      });

      let error;
      try {
        await drone.save();
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
    });
  });

  describe('Drone Battery Management', () => {
    it('should update battery level', async () => {
      const drone = await Drone.create({
        shop: shopId,
        model: 'DJI Air 2S',
        serialNumber: 'DJI-A2S-001',
        capacity: {
          weight: 4.8,
          volume: 7000,
        },
        battery: {
          current: 100,
          maxCapacity: 3500,
        },
        specifications: {
          maxSpeed: 68,
          maxAltitude: 120,
          maxAltitude: 120,
          range: 27,
          flightTime: 31,
        },
      });

      drone.battery.current = 45;
      await drone.save();

      const updatedDrone = await Drone.findById(drone._id);
      expect(updatedDrone.battery.current).toBe(45);
    });

    it('should not allow battery below 0', async () => {
      const drone = new Drone({
        shop: shopId,
        model: 'DJI FPV',
        serialNumber: 'DJI-FPV-001',
        capacity: {
          weight: 5.0,
          volume: 7500,
        },
        battery: {
          current: -10,
          maxCapacity: 2000,
        },
        specifications: {
          maxSpeed: 140,
          maxAltitude: 120,
          range: 16,
          flightTime: 20,
        },
      });

      let error;
      try {
        await drone.save();
      } catch (err) {
        error = err;
      }

      expect(error).toBeDefined();
    });
  });

  describe('Drone Queries', () => {
    beforeEach(async () => {
      await Drone.create([
        {
          shop: shopId,
          model: 'Drone A',
          serialNumber: 'A-001',
          capacity: { weight: 5, volume: 8000 },
          battery: { current: 100, maxCapacity: 5000 },
          status: 'available',
          specifications: { maxSpeed: 70,
          maxAltitude: 120, range: 30, flightTime: 25 },
        },
        {
          shop: shopId,
          model: 'Drone B',
          serialNumber: 'B-001',
          capacity: { weight: 3, volume: 5000 },
          battery: { current: 50, maxCapacity: 3000 },
          status: 'busy',
          specifications: { maxSpeed: 60,
          maxAltitude: 120, range: 20, flightTime: 30 },
        },
      ]);
    });

    it('should find available drones', async () => {
      const availableDrones = await Drone.find({ status: 'available' });

      expect(availableDrones).toHaveLength(1);
      expect(availableDrones[0].model).toBe('Drone A');
    });

    it('should find drones by shop', async () => {
      const shopDrones = await Drone.find({ shop: shopId });

      expect(shopDrones).toHaveLength(2);
    });
  });
});
