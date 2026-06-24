const express  = require('express');
const { Op }   = require('sequelize');
const router   = express.Router();

const User                           = require('../models/User');
const { authMiddleware }             = require('../middleware/auth');
const { upload, uploadToCloudinary } = require('../middleware/upload');

// ─── GET /api/users/donors — Available donors (optional bloodGroup filter) ───
router.get('/donors', authMiddleware, async (req, res) => {
  try {
    const { bloodGroup } = req.query;
    const where = { availability: true, is_admin: false };
    if (bloodGroup && bloodGroup !== 'All') {
      where.blood_group = bloodGroup;
    }

    const donors = await User.findAll({
      where,
      attributes: { exclude: ['password_hash', 'nid_image_front', 'nid_image_back'] },
      order: [['created_at', 'DESC']]
    });

    res.json(donors);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch donors', message: err.message });
  }
});

// ─── GET /api/users/me — Current user profile ────────────────────────────────
router.get('/me', authMiddleware, async (req, res) => {
  res.json(req.user);
});

// ─── PUT /api/users/me — Update address / availability ───────────────────────
router.put('/me', authMiddleware, async (req, res) => {
  try {
    const { address, availability } = req.body;
    const updates = {};
    if (address     !== undefined) updates.address      = address;
    if (availability !== undefined) updates.availability = availability;

    await User.update(updates, { where: { id: req.user.id } });
    const updated = await User.findByPk(req.user.id, {
      attributes: { exclude: ['password_hash'] }
    });

    res.json({ message: 'Profile updated!', user: updated });
  } catch (err) {
    res.status(500).json({ error: 'Update failed', message: err.message });
  }
});

// ─── PUT /api/users/me/avatar — Update profile photo ─────────────────────────
router.put(
  '/me/avatar',
  authMiddleware,
  upload.single('profile_image'),
  async (req, res) => {
    try {
      if (!req.file) return res.status(400).json({ error: 'No image provided' });

      const url = await uploadToCloudinary(
        req.file.buffer, 'profiles', `profile_${req.user.id}`
      );

      await User.update({ profile_image: url }, { where: { id: req.user.id } });
      res.json({ message: 'Avatar updated!', profile_image: url });
    } catch (err) {
      res.status(500).json({ error: 'Avatar update failed', message: err.message });
    }
  }
);

module.exports = router;
