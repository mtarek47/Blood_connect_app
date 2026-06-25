package com.example.data.model

import java.io.Serializable

// Room annotations সরানো হয়েছে — এখন server থেকে data আসে
data class User(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String,
    val bloodGroup: String,
    val gender: String = "",
    val dob: String = "",
    val nidImageFront: String? = null,
    val nidImageBack: String? = null,
    val profileImage: String? = null,
    val passwordHash: String = "",
    val isVerified: Boolean = false,
    val isAdmin: Boolean = false,
    val availability: Boolean = true
) : Serializable
