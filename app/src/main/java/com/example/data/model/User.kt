package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "users",
    indices = [Index(value = ["phone"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String,
    @ColumnInfo(name = "blood_group") val bloodGroup: String,
    @ColumnInfo(name = "nid_image_front") val nidImageFront: String? = null,
    @ColumnInfo(name = "nid_image_back") val nidImageBack: String? = null,
    @ColumnInfo(name = "profile_image") val profileImage: String? = null,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "is_verified") val isVerified: Boolean = false,
    @ColumnInfo(name = "is_admin") val isAdmin: Boolean = false,
    val availability: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) : Serializable
