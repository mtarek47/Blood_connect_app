require('dotenv').config();
const express = require('express');
const cors = require('cors');

// Routes
const authRoutes         = require('./routes/auth');
const userRoutes         = require('./routes/users');
const bloodRequestRoutes = require('./routes/bloodRequests');
const donationRoutes     = require('./routes/donations');
const adminRoutes        = require('./routes/admin');

// DB sync
const { sequelize } = require('./config/database');

const app = express();

// ─── Middleware ───────────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ─── Routes ──────────────────────────────────────────────────────────────────
app.use('/api/auth',           authRoutes);
app.use('/api/users',          userRoutes);
app.use('/api/blood-requests', bloodRequestRoutes);
app.use('/api/donations',      donationRoutes);
app.use('/api/admin',          adminRoutes);

// Health check
app.get('/', (req, res) => {
  res.json({ status: 'Blood Connect API is running 🩸', version: '1.0.0' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Internal server error', message: err.message });
});

// ─── Start ───────────────────────────────────────────────────────────────────
const PORT = process.env.PORT || 3000;

sequelize.sync({ alter: true })
  .then(() => {
    console.log('✅ MySQL connected & tables synced');
    app.listen(PORT, () => {
      console.log(`🚀 Blood Connect API running on port ${PORT}`);
    });
  })
  .catch(err => {
    console.error('❌ DB connection failed:', err.message);
    process.exit(1);
  });
