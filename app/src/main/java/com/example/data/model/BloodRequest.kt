package com.example.data.model

import java.io.Serializable

data class BloodRequest(
    val id: Int = 0,
    val recipientId: Int,
    val recipientName: String,
    val recipientPhone: String,
    val bloodGroup: String,
    val gender: String,
    val age: String,
    val location: String,
    val hospitalName: String? = null,
    val urgencyLevel: String,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
