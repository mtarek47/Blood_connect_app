const express = require('express');
const router  = express.Router();

const Donation           = require('../models/Donation');
const { authMiddleware } = require('../middleware/auth');

// ─── POST /api/donations — Respond to a blood request ────────────────────────
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { request_id } = req.body;
    if (!request_id) return res.status(400).json({ error: 'request_id is required' });

    const donation = await Donation.create({
      donor_id:            req.user.id,
      donor_name:          req.user.name,
      donor_phone:         req.user.phone,
      donor_profile_image: req.user.profile_image || null,
      request_id,
      status: 'accepted'
    });

    res.status(201).json({
      message: 'Thank you! Contact details shared with the recipient.',
      donation
    });
  } catch (err) {
    res.status(500).json({ error: 'Failed to respond', message: err.message });
  }
});

// ─── GET /api/donations/request/:requestId — Get donors for a request ────────
router.get('/request/:requestId', authMiddleware, async (req, res) => {
  try {
    const donations = await Donation.findAll({
      where: { request_id: req.params.requestId },
      order: [['timestamp', 'DESC']]
    });
    res.json(donations);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch donations', message: err.message });
  }
});

module.exports = router;
