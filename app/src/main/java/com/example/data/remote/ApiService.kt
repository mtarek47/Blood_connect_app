package com.example.data.remote

import com.example.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────
    @Multipart
    @POST("auth/register")
    suspend fun register(
        @Part("name")        name:       RequestBody,
        @Part("phone")       phone:      RequestBody,
        @Part("address")     address:    RequestBody,
        @Part("blood_group") bloodGroup: RequestBody,
        @Part("gender")      gender:     RequestBody,
        @Part("dob")         dob:        RequestBody,
        @Part("password")    password:   RequestBody,
        @Part profileImage:  MultipartBody.Part?,
        @Part nidFront:      MultipartBody.Part?,
        @Part nidBack:       MultipartBody.Part?
    ): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponseDto>

    // ─── Users ────────────────────────────────────────────────────────────────
    @GET("users/donors")
    suspend fun getDonors(
        @Query("bloodGroup") bloodGroup: String? = null
    ): Response<List<UserDto>>

    @GET("users/me")
    suspend fun getMe(): Response<UserDto>

    @PUT("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileBody): Response<MessageResponse>

    @PUT("users/me/password")
    suspend fun changePassword(@Body body: UpdatePasswordBody): Response<MessageResponse>

    @Multipart
    @PUT("users/me/avatar")
    suspend fun updateProfileImage(@Part profile_image: MultipartBody.Part): Response<MessageResponse>

    // ─── Blood Requests ───────────────────────────────────────────────────────
    @GET("blood-requests/active")
    suspend fun getActiveRequests(): Response<List<BloodRequestDto>>

    @GET("blood-requests/my")
    suspend fun getMyRequests(): Response<List<BloodRequestDto>>

    @POST("blood-requests")
    suspend fun createBloodRequest(@Body body: BloodRequestBody): Response<MessageResponse>

    @PUT("blood-requests/{id}/complete")
    suspend fun completeRequest(@Path("id") id: Int): Response<MessageResponse>

    // ─── Donations ────────────────────────────────────────────────────────────
    @POST("donations")
    suspend fun respondToRequest(@Body body: DonationRequest): Response<MessageResponse>

    @GET("donations/request/{requestId}")
    suspend fun getDonationsForRequest(
        @Path("requestId") requestId: Int
    ): Response<List<DonationDto>>

    // ─── Admin ────────────────────────────────────────────────────────────────
    @GET("admin/unverified")
    suspend fun getUnverifiedUsers(): Response<List<UserDto>>

    @GET("admin/users")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @PUT("admin/verify/{id}")
    suspend fun verifyUser(@Path("id") id: Int): Response<MessageResponse>

    @DELETE("admin/user/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<MessageResponse>
}
