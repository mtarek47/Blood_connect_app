/**
 * Admin seed script — একবার run করলেই হবে
 * Usage: node scripts/createAdmin.js
 */
require('dotenv').config({ path: require('path').join(__dirname, '..', '.env') });

const bcrypt   = require('bcryptjs');
const User     = require('../models/User');
const { sequelize } = require('../config/database');

const ADMIN_PHONE    = 'admin';
const ADMIN_PASSWORD = 'admin123';

async function createAdmin() {
  try {
    await sequelize.authenticate();
    await sequelize.sync({ alter: true });

    const existing = await User.findOne({ where: { phone: ADMIN_PHONE } });
    if (existing) {
      console.log('⚠️  Admin already exists! Phone:', ADMIN_PHONE);
      process.exit(0);
    }

    const password_hash = await bcrypt.hash(ADMIN_PASSWORD, 12);
    const admin = await User.create({
      name:          'Admin',
      phone:         ADMIN_PHONE,
      address:       'Blood Connect HQ',
      blood_group:   'O+',
      password_hash,
      is_verified:   true,
      is_admin:      true,
      availability:  false
    });

    console.log('✅ Admin created successfully!');
    console.log('   Phone   :', ADMIN_PHONE);
    console.log('   Password:', ADMIN_PASSWORD);
    console.log('   ID      :', admin.id);
    process.exit(0);
  } catch (err) {
    console.error('❌ Failed:', err.message);
    process.exit(1);
  }
}

createAdmin();
