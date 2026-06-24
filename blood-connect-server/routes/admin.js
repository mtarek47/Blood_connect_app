const express = require('express');
const bcrypt  = require('bcryptjs');
const router  = express.Router();

const User                          = require('../models/User');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');

// All admin routes require auth + admin role
router.use(authMiddleware, adminMiddleware);

// ─── GET /api/admin/unverified — Pending NID review ──────────────────────────
router.get('/unverified', async (req, res) => {
  try {
    const users = await User.findAll({
      where:  { is_verified: false, is_admin: false },
      order:  [['created_at', 'DESC']]
    });
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch unverified users', message: err.message });
  }
});

// ─── GET /api/admin/users — All users ────────────────────────────────────────
router.get('/users', async (req, res) => {
  try {
    const users = await User.findAll({
      attributes: { exclude: ['password_hash'] },
      order: [['created_at', 'DESC']]
    });
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch users', message: err.message });
  }
});

// ─── PUT /api/admin/verify/:id — Approve NID ─────────────────────────────────
router.put('/verify/:id', async (req, res) => {
  try {
    const user = await User.findByPk(req.params.id);
    if (!user) return res.status(404).json({ error: 'User not found' });

    await user.update({ is_verified: true });
    res.json({ message: `NID verified for ${user.name}!`, user });
  } catch (err) {
    res.status(500).json({ error: 'Verification failed', message: err.message });
  }
});

// ─── POST /api/admin/seed — Create initial admin (run once) ──────────────────
router.post('/seed', async (req, res) => {
  try {
    const { adminPhone, adminPassword } = req.body;
    if (!adminPhone || !adminPassword) {
      return res.status(400).json({ error: 'adminPhone and adminPassword required' });
    }

    const existing = await User.findOne({ where: { phone: adminPhone } });
    if (existing) return res.status(409).json({ error: 'Admin already exists' });

    const password_hash = await bcrypt.hash(adminPassword, 12);
    const admin = await User.create({
      name: 'Admin',
      phone: adminPhone,
      address: 'Blood Connect HQ',
      blood_group: 'O+',
      password_hash,
      is_verified: true,
      is_admin: true,
      availability: false
    });

    res.status(201).json({ message: 'Admin created!', id: admin.id });
  } catch (err) {
    res.status(500).json({ error: 'Seed failed', message: err.message });
  }
});

module.exports = router;
