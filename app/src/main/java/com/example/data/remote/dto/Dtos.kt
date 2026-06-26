package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── User DTO ────────────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id")              val id: Int,
    @Json(name = "name")            val name: String,
    @Json(name = "phone")           val phone: String,
    @Json(name = "address")         val address: String,
    @Json(name = "blood_group")     val bloodGroup: String,
    @Json(name = "gender")          val gender: String?,
    @Json(name = "dob")             val dob: String?,
    @Json(name = "profile_image")   val profileImage: String?,
    @Json(name = "nid_image_front") val nidImageFront: String?,
    @Json(name = "nid_image_back")  val nidImageBack: String?,
    @Json(name = "is_verified")     val isVerified: Boolean,
    @Json(name = "is_admin")        val isAdmin: Boolean,
    @Json(name = "availability")    val availability: Boolean
)

// ─── Auth Responses ───────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "message") val message: String,
    @Json(name = "token")   val token: String,
    @Json(name = "user")    val user: UserDto
)

// ─── BloodRequest DTO ─────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class BloodRequestDto(
    @Json(name = "id")             val id: Int,
    @Json(name = "recipient_id")   val recipientId: Int,
    @Json(name = "recipient_name") val recipientName: String,
    @Json(name = "recipient_phone")val recipientPhone: String,
    @Json(name = "blood_group")    val bloodGroup: String,
    @Json(name = "gender")         val gender: String,
    @Json(name = "age")            val age: String,
    @Json(name = "location")       val location: String,
    @Json(name = "hospital_name")  val hospitalName: String?,
    @Json(name = "urgency_level")  val urgencyLevel: String,
    @Json(name = "status")         val status: String,
    @Json(name = "created_at")     val createdAt: String?
)

// ─── Donation DTO ─────────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class DonationDto(
    @Json(name = "id")                   val id: Int,
    @Json(name = "donor_id")             val donorId: Int,
    @Json(name = "donor_name")           val donorName: String,
    @Json(name = "donor_phone")          val donorPhone: String,
    @Json(name = "donor_profile_image")  val donorProfileImage: String?,
    @Json(name = "request_id")           val requestId: Int,
    @Json(name = "status")               val status: String,
    @Json(name = "timestamp")            val timestamp: String?
)

// ─── Request bodies ───────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "phone")    val phone: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class BloodRequestBody(
    @Json(name = "blood_group")    val bloodGroup: String,
    @Json(name = "gender")         val gender: String,
    @Json(name = "age")            val age: String,
    @Json(name = "location")       val location: String,
    @Json(name = "hospital_name")  val hospitalName: String?,
    @Json(name = "urgency_level")  val urgencyLevel: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileBody(
    @Json(name = "address")      val address: String?,
    @Json(name = "availability") val availability: Boolean?
)

@JsonClass(generateAdapter = true)
data class UpdatePasswordBody(
    @Json(name = "old_password") val oldPassword: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class DonationRequest(
    @Json(name = "request_id") val requestId: Int
)

// ─── Generic message response ─────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "message") val message: String
)

// ─── Password Recovery DTOs ───────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class RecoveryRequestReq(
    @Json(name = "phone")     val phone: String,
    @Json(name = "nidNumber") val nidNumber: String
)

@JsonClass(generateAdapter = true)
data class RecoveryUserDto(
    @Json(name = "name")            val name: String?,
    @Json(name = "profile_image")   val profileImage: String?,
    @Json(name = "nid_image_front") val nidImageFront: String?,
    @Json(name = "nid_image_back")  val nidImageBack: String?
)

@JsonClass(generateAdapter = true)
data class RecoveryRequestDto(
    @Json(name = "id")         val id: Int,
    @Json(name = "phone")      val phone: String,
    @Json(name = "nid_number") val nidNumber: String,
    @Json(name = "status")     val status: String,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "User")       val user: RecoveryUserDto?
)
