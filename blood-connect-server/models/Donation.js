const { DataTypes } = require('sequelize');
const { sequelize }  = require('../config/database');

const Donation = sequelize.define('Donation', {
  id: {
    type: DataTypes.INTEGER,
    autoIncrement: true,
    primaryKey: true
  },
  donor_id: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  donor_name: {
    type: DataTypes.STRING(100),
    allowNull: false
  },
  donor_phone: {
    type: DataTypes.STRING(20),
    allowNull: false
  },
  donor_profile_image: {
    type: DataTypes.TEXT,   // Cloudinary URL
    allowNull: true
  },
  request_id: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  status: {
    type: DataTypes.ENUM('accepted', 'rejected', 'completed'),
    defaultValue: 'accepted'
  }
}, {
  tableName: 'donations',
  timestamps: true,
  createdAt: 'timestamp',
  updatedAt: false
});

module.exports = Donation;
