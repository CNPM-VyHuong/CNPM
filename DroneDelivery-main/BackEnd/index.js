import express from "express";
import dotenv from "dotenv";
dotenv.config();
import { createServer } from "http";
import { Server } from "socket.io";
import connectDB from "./config/db.js";
import cookieParser from "cookie-parser";
import authRouter from "./routes/auth.routes.js";
import userRouter from "./routes/user.routes.js";
import shopRouter from "./routes/shop.routes.js";
import itemRouter from "./routes/item.routes.js";
import orderRouter from "./routes/order.routes.js";
import cartRouter from "./routes/cart.routes.js";
import roleRouter from "./routes/role.routes.js";
import paymentRouter from "./routes/payment.routes.js";
import deliveryRouter from "./routes/delivery.routes.js";
import droneRouter from "./routes/drone.routes.js";
import locationRouter from "./routes/location.routes.js";
import adminRouter from "./routes/admin.routes.js";
import reportRouter from "./routes/report.routes.js";
import cors from "cors";
import client from "prom-client";
import Drone from "./models/drone.model.js";

// Prometheus metrics setup
const register = new client.Registry();
client.collectDefaultMetrics({ register });

// Custom metrics
const httpRequestDuration = new client.Histogram({
  name: 'http_request_duration_seconds',
  help: 'Duration of HTTP requests in seconds',
  labelNames: ['method', 'route', 'status_code'],
  buckets: [0.1, 0.5, 1, 2, 5],
  registers: [register],
});

const httpRequestTotal = new client.Counter({
  name: 'http_requests_total',
  help: 'Total number of HTTP requests',
  labelNames: ['method', 'route', 'status_code'],
  registers: [register],
});

const activeConnections = new client.Gauge({
  name: 'active_connections',
  help: 'Number of active connections',
  registers: [register],
});

const droneStatusGauge = new client.Gauge({
  name: 'drone_status',
  help: 'Drone status by type',
  labelNames: ['status'],
  registers: [register],
});

// Function to update drone status metrics
async function updateDroneMetrics() {
  try {
    const statuses = ['available', 'busy', 'maintenance', 'offline', 'retired'];
    for (const status of statuses) {
      const count = await Drone.countDocuments({ status });
      droneStatusGauge.labels(status).set(count);
    }
  } catch (error) {
    console.error('Error updating drone metrics:', error);
  }
}

const app = express();
const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: {
    origin: true, // Allow all origins for development
    credentials: true,
  },
});

const PORT = process.env.PORT || 8000;

// Make io accessible to routes
app.set("io", io);

// Metrics middleware
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = (Date.now() - start) / 1000;
    httpRequestDuration.labels(req.method, req.route?.path || req.path, res.statusCode).observe(duration);
    httpRequestTotal.labels(req.method, req.route?.path || req.path, res.statusCode).inc();
  });
  next();
});

app.use(
  cors({
    origin: true, // Allow all origins for development
    credentials: true,
  })
);
app.use(express.json());
app.use(cookieParser());

// Metrics endpoint
app.get('/metrics', async (req, res) => {
  res.set('Content-Type', register.contentType);
  res.end(await register.metrics());
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
  });
});

app.use("/api/auth", authRouter);
app.use("/api/user", userRouter);
app.use("/api/shop", shopRouter);
app.use("/api/item", itemRouter);
app.use("/api/order", orderRouter);
app.use("/api/cart", cartRouter);
app.use("/api/role", roleRouter);
app.use("/api/payment", paymentRouter);
app.use("/api/delivery", deliveryRouter);
app.use("/api/drone", droneRouter);
app.use("/api/location", locationRouter);
app.use("/api/admin", adminRouter);
app.use("/api/report", reportRouter);

// Socket.io connection handling
io.on("connection", (socket) => {
  console.log("Client connected:", socket.id);
  activeConnections.inc();

  // Join room for specific order tracking
  socket.on("join-order", (orderId) => {
    socket.join(`order-${orderId}`);
    console.log(`Socket ${socket.id} joined order-${orderId}`);
  });

  // Leave order room
  socket.on("leave-order", (orderId) => {
    socket.leave(`order-${orderId}`);
    console.log(`Socket ${socket.id} left order-${orderId}`);
  });

  // Drone location update from simulator
  socket.on("drone-location-update", (data) => {
    const { orderId, location } = data;
    // Broadcast to all clients tracking this order
    io.to(`order-${orderId}`).emit("drone-location", {
      orderId,
      location,
      timestamp: new Date(),
    });
  });

  socket.on("disconnect", () => {
    console.log("Client disconnected:", socket.id);
    activeConnections.dec();
  });
});

httpServer.listen(PORT, () => {
  connectDB();
  console.log(`Server is running on port ${PORT}`);
  console.log(`Socket.io server ready`);
  console.log(`Metrics available at http://localhost:${PORT}/metrics`);
  console.log(`Health check at http://localhost:${PORT}/health`);
  
  // Update drone metrics every 30 seconds
  updateDroneMetrics(); // Initial update
  setInterval(updateDroneMetrics, 30000);
});

export { app, register, httpRequestDuration, httpRequestTotal, activeConnections, droneStatusGauge };

