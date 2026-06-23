package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "blood_requests")
data class BloodRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "recipient_id") val recipientId: Int,
    @ColumnInfo(name = "recipient_name") val recipientName: String,
    @ColumnInfo(name = "recipient_phone") val recipientPhone: String,
    @ColumnInfo(name = "blood_group") val bloodGroup: String,
    val location: String,
    @ColumnInfo(name = "hospital_name") val hospitalName: String? = null,
    @ColumnInfo(name = "urgency_level") val urgencyLevel: String, // "Normal", "Urgent", "Critical"
    val status: String = "active", // "active", "completed"
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) : Serializable
