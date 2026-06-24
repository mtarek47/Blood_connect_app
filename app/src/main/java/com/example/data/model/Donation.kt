package com.example.data.model

import java.io.Serializable

data class Donation(
    val id: Int = 0,
    val donorId: Int,
    val donorName: String,
    val donorPhone: String,
    val donorProfileImage: String? = null,
    val requestId: Int,
    val status: String = "accepted",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
