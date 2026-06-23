package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "donor_id") val donorId: Int,
    @ColumnInfo(name = "donor_name") val donorName: String,
    @ColumnInfo(name = "donor_phone") val donorPhone: String,
    @ColumnInfo(name = "donor_profile_image") val donorProfileImage: String? = null,
    @ColumnInfo(name = "request_id") val requestId: Int,
    val status: String = "accepted", // "accepted", "rejected", "completed"
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
