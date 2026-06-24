const { DataTypes } = require('sequelize');
const { sequelize }  = require('../config/database');

const BloodRequest = sequelize.define('BloodRequest', {
  id: {
    type: DataTypes.INTEGER,
    autoIncrement: true,
    primaryKey: true
  },
  recipient_id: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  recipient_name: {
    type: DataTypes.STRING(100),
    allowNull: false
  },
  recipient_phone: {
    type: DataTypes.STRING(20),
    allowNull: false
  },
  blood_group: {
    type: DataTypes.ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'),
    allowNull: false
  },
  location: {
    type: DataTypes.STRING(255),
    allowNull: false
  },
  hospital_name: {
    type: DataTypes.STRING(150),
    allowNull: true
  },
  urgency_level: {
    type: DataTypes.ENUM('Normal', 'Urgent', 'Critical'),
    defaultValue: 'Normal'
  },
  status: {
    type: DataTypes.ENUM('active', 'completed'),
    defaultValue: 'active'
  }
}, {
  tableName: 'blood_requests',
  timestamps: true,
  createdAt: 'created_at',
  updatedAt: 'updated_at'
});

module.exports = BloodRequest;
