const express = require('express');
const router  = express.Router();

const BloodRequest       = require('../models/BloodRequest');
const { authMiddleware } = require('../middleware/auth');

// ─── GET /api/blood-requests/active ──────────────────────────────────────────
router.get('/active', authMiddleware, async (req, res) => {
  try {
    const requests = await BloodRequest.findAll({
      where:  { status: 'active' },
      order:  [['created_at', 'DESC']]
    });
    res.json(requests);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch requests', message: err.message });
  }
});

// ─── GET /api/blood-requests/my ──────────────────────────────────────────────
router.get('/my', authMiddleware, async (req, res) => {
  try {
    const requests = await BloodRequest.findAll({
      where: { recipient_id: req.user.id },
      order: [['created_at', 'DESC']]
    });
    res.json(requests);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch requests', message: err.message });
  }
});

// ─── POST /api/blood-requests ─────────────────────────────────────────────────
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { blood_group, location, hospital_name, urgency_level } = req.body;

    if (!blood_group || !location || !urgency_level) {
      return res.status(400).json({ error: 'blood_group, location and urgency_level are required' });
    }

    const request = await BloodRequest.create({
      recipient_id:    req.user.id,
      recipient_name:  req.user.name,
      recipient_phone: req.user.phone,
      blood_group,
      location,
      hospital_name:   hospital_name || null,
      urgency_level,
      status:          'active'
    });

    res.status(201).json({ message: 'Blood request created!', request });
  } catch (err) {
    res.status(500).json({ error: 'Failed to create request', message: err.message });
  }
});

// ─── PUT /api/blood-requests/:id/complete ────────────────────────────────────
router.put('/:id/complete', authMiddleware, async (req, res) => {
  try {
    const request = await BloodRequest.findByPk(req.params.id);
    if (!request) return res.status(404).json({ error: 'Request not found' });
    if (request.recipient_id !== req.user.id && !req.user.is_admin) {
      return res.status(403).json({ error: 'Not authorized' });
    }

    await request.update({ status: 'completed' });
    res.json({ message: 'Request marked as completed!', request });
  } catch (err) {
    res.status(500).json({ error: 'Failed to update request', message: err.message });
  }
});

module.exports = router;
