const express  = require('express');
const bcrypt   = require('bcryptjs');
const jwt      = require('jsonwebtoken');
const router   = express.Router();

const User                        = require('../models/User');
const { upload, uploadToCloudinary } = require('../middleware/upload');

// ─── POST /api/auth/register ──────────────────────────────────────────────────
// Fields: name, phone, address, blood_group, password
// Files:  profile_image, nid_image_front, nid_image_back
router.post(
  '/register',
  upload.fields([
    { name: 'profile_image',   maxCount: 1 },
    { name: 'nid_image_front', maxCount: 1 },
    { name: 'nid_image_back',  maxCount: 1 }
  ]),
  async (req, res) => {
    try {
      const { name, phone, address, blood_group, password } = req.body;

      // Validation
      if (!name || !phone || !address || !blood_group || !password) {
        return res.status(400).json({ error: 'All fields are required' });
      }

      // Duplicate check
      const existing = await User.findOne({ where: { phone } });
      if (existing) {
        return res.status(409).json({ error: 'Phone number already registered' });
      }

      // Hash password
      const password_hash = await bcrypt.hash(password, 12);

      // Upload images to Cloudinary (if provided)
      let profileImageUrl   = null;
      let nidFrontUrl       = null;
      let nidBackUrl        = null;
      const userId          = `user_${phone.replace(/\D/g, '')}_${Date.now()}`;

      if (req.files?.profile_image?.[0]) {
        profileImageUrl = await uploadToCloudinary(
          req.files.profile_image[0].buffer, 'profiles', `profile_${userId}`
        );
      }
      if (req.files?.nid_image_front?.[0]) {
        nidFrontUrl = await uploadToCloudinary(
          req.files.nid_image_front[0].buffer, 'nids', `nid_front_${userId}`
        );
      }
      if (req.files?.nid_image_back?.[0]) {
        nidBackUrl = await uploadToCloudinary(
          req.files.nid_image_back[0].buffer, 'nids', `nid_back_${userId}`
        );
      }

      // Create user
      const user = await User.create({
        name,
        phone,
        address,
        blood_group,
        password_hash,
        profile_image:   profileImageUrl,
        nid_image_front: nidFrontUrl,
        nid_image_back:  nidBackUrl,
        is_verified:     false,
        is_admin:        false,
        availability:    true
      });

      // Generate token
      const token = jwt.sign(
        { userId: user.id, isAdmin: user.is_admin },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || '30d' }
      );

      res.status(201).json({
        message: 'Registration successful! NID pending verification.',
        token,
        user: {
          id:              user.id,
          name:            user.name,
          phone:           user.phone,
          address:         user.address,
          blood_group:     user.blood_group,
          profile_image:   user.profile_image,
          nid_image_front: user.nid_image_front,
          nid_image_back:  user.nid_image_back,
          is_verified:     user.is_verified,
          is_admin:        user.is_admin,
          availability:    user.availability
        }
      });
    } catch (err) {
      console.error('Register error:', err);
      res.status(500).json({ error: 'Registration failed', message: err.message });
    }
  }
);

// ─── POST /api/auth/login ─────────────────────────────────────────────────────
router.post('/login', async (req, res) => {
  try {
    const { phone, password } = req.body;

    if (!phone || !password) {
      return res.status(400).json({ error: 'Phone and password are required' });
    }

    const user = await User.findOne({ where: { phone } });
    if (!user) {
      return res.status(401).json({ error: 'Phone number not found' });
    }

    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      return res.status(401).json({ error: 'Incorrect password' });
    }

    const token = jwt.sign(
      { userId: user.id, isAdmin: user.is_admin },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '30d' }
    );

    res.json({
      message: `Welcome back, ${user.name}!`,
      token,
      user: {
        id:              user.id,
        name:            user.name,
        phone:           user.phone,
        address:         user.address,
        blood_group:     user.blood_group,
        profile_image:   user.profile_image,
        nid_image_front: user.nid_image_front,
        nid_image_back:  user.nid_image_back,
        is_verified:     user.is_verified,
        is_admin:        user.is_admin,
        availability:    user.availability
      }
    });
  } catch (err) {
    console.error('Login error:', err);
    res.status(500).json({ error: 'Login failed', message: err.message });
  }
});

module.exports = router;
