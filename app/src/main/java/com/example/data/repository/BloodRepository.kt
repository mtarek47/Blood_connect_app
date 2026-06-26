package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.SessionManager
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.User
import com.example.data.remote.ApiClient
import com.example.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class BloodRepository(private val context: Context) {

    private val api = ApiClient.apiService
    val sessionManager = SessionManager(context)

    // In-memory cache flows (UI observe করে)
    private val _activeRequests   = MutableStateFlow<List<BloodRequest>>(emptyList())
    val activeRequests: Flow<List<BloodRequest>> = _activeRequests.asStateFlow()

    private val _allUsers         = MutableStateFlow<List<User>>(emptyList())
    val allUsers: Flow<List<User>> = _allUsers.asStateFlow()

    private val _unverifiedUsers  = MutableStateFlow<List<User>>(emptyList())
    val unverifiedUsers: Flow<List<User>> = _unverifiedUsers.asStateFlow()

    // ─── Mappers: DTO → Domain model ────────────────────────────────────────────
    private fun UserDto.toUser() = User(
        id            = id,
        name          = name,
        phone         = phone,
        address       = address,
        bloodGroup    = bloodGroup,
        gender        = gender ?: "",
        dob           = dob ?: "",
        profileImage  = profileImage,
        nidImageFront = nidImageFront,
        nidImageBack  = nidImageBack,
        isVerified    = isVerified,
        isAdmin       = isAdmin,
        availability  = availability,
        passwordHash  = ""   // never sent from server
    )

    private fun BloodRequestDto.toBloodRequest() = BloodRequest(
        id             = id,
        recipientId    = recipientId,
        recipientName  = recipientName,
        recipientPhone = recipientPhone,
        bloodGroup     = bloodGroup,
        gender         = gender,
        age            = age,
        location       = location,
        hospitalName   = hospitalName,
        urgencyLevel   = urgencyLevel,
        status         = status
    )

    private fun DonationDto.toDonation() = Donation(
        id                = id,
        donorId           = donorId,
        donorName         = donorName,
        donorPhone        = donorPhone,
        donorProfileImage = donorProfileImage,
        requestId         = requestId,
        status            = status
    )

    // ─── Auth ────────────────────────────────────────────────────────────────────
    suspend fun registerUser(
        name: String, phone: String, address: String,
        bloodGroup: String, gender: String, dob: String, password: String,
        profileImageUri: String?, nidFrontUri: String?, nidBackUri: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val toBody = { s: String -> s.toRequestBody("text/plain".toMediaTypeOrNull()) }
            val toFilePart = { uri: String?, fieldName: String ->
                uri?.let {
                    val file = uriToTempFile(Uri.parse(it)) ?: return@let null
                    val reqBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(fieldName, file.name, reqBody)
                }
            }

            val response = api.register(
                name       = toBody(name),
                phone      = toBody(phone),
                address    = toBody(address),
                bloodGroup = toBody(bloodGroup),
                gender     = toBody(gender),
                dob        = toBody(dob),
                password   = toBody(password),
                profileImage = toFilePart(profileImageUri, "profile_image"),
                nidFront     = toFilePart(nidFrontUri,    "nid_image_front"),
                nidBack      = toFilePart(nidBackUri,     "nid_image_back")
            )

            if (response.isSuccessful) {
                val body = response.body()!!
                sessionManager.saveSession(body.token, body.user.id)
                Result.success(body.user.toUser())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(phone: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(phone, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    sessionManager.saveSession(body.token, body.user.id)
                    Result.success(body.user.toUser())
                } else {
                    Result.failure(Exception("Incorrect phone or password"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Connection failed. Check your internet."))
            }
        }

    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                Result.success(response.body()!!.toUser())
            } else {
                Result.failure(Exception("Failed to load user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() = sessionManager.clearSession()

    // ─── Users / Donors ──────────────────────────────────────────────────────────
    fun getAvailableDonors(bloodGroup: String): Flow<List<User>> {
        val flow = MutableStateFlow<List<User>>(emptyList())
        return flow  // ViewModel coroutine-এ refreshDonors() call করে
    }

    suspend fun refreshDonors(bloodGroup: String = "All"): List<User> =
        withContext(Dispatchers.IO) {
            try {
                val bg = if (bloodGroup == "All") null else bloodGroup
                val response = api.getDonors(bg)
                if (response.isSuccessful) response.body()?.map { it.toUser() } ?: emptyList()
                else emptyList()
            } catch (e: Exception) { emptyList() }
        }

    suspend fun updateUser(address: String? = null, availability: Boolean? = null): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateProfile(UpdateProfileBody(address, availability))
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Update failed"))
            } catch (e: Exception) { Result.failure(e) }
        }

    suspend fun changePassword(oldPass: String, newPass: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.changePassword(UpdatePasswordBody(oldPass, newPass))
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception(response.errorBody()?.string() ?: "Password update failed"))
            } catch (e: Exception) { Result.failure(e) }
        }

    suspend fun updateProfileImage(uri: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val file = uriToTempFile(Uri.parse(uri)) ?: return@withContext Result.failure(Exception("Could not load image"))
                val reqBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("profile_image", file.name, reqBody)
                val response = api.updateProfileImage(part)

                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to upload avatar"))
            } catch (e: Exception) { Result.failure(e) }
        }

    // ─── Blood Requests ──────────────────────────────────────────────────────────
    suspend fun refreshActiveRequests() = withContext(Dispatchers.IO) {
        try {
            val response = api.getActiveRequests()
            if (response.isSuccessful) {
                _activeRequests.value = response.body()?.map { it.toBloodRequest() } ?: emptyList()
            }
        } catch (e: Exception) { /* keep cached */ }
    }

    suspend fun createBloodRequest(
        bloodGroup: String, gender: String, age: String, location: String, hospitalName: String, urgencyLevel: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.createBloodRequest(
                BloodRequestBody(bloodGroup, gender, age, location, hospitalName.ifBlank { null }, urgencyLevel)
            )
            if (response.isSuccessful) {
                refreshActiveRequests()
                Result.success(Unit)
            } else Result.failure(Exception("Failed to create request"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun completeRequest(requestId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.completeRequest(requestId)
            if (response.isSuccessful) {
                refreshActiveRequests()
                Result.success(Unit)
            } else Result.failure(Exception("Failed to complete request"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteBloodRequest(requestId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteBloodRequest(requestId)
            if (response.isSuccessful) {
                refreshActiveRequests()
                Result.success(Unit)
            } else Result.failure(Exception("Failed to cancel request"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun ignoreRequest(requestId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.ignoreRequest(requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else Result.failure(Exception("Failed to ignore request"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Donations ───────────────────────────────────────────────────────────────
    suspend fun respondToRequest(requestId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.respondToRequest(DonationRequest(requestId))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to respond"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getDonationsForRequest(requestId: Int): List<Donation> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getDonationsForRequest(requestId)
                if (response.isSuccessful) response.body()?.map { it.toDonation() } ?: emptyList()
                else emptyList()
            } catch (e: Exception) { emptyList() }
        }

    // ─── Admin ───────────────────────────────────────────────────────────────────
    suspend fun refreshUnverifiedUsers() = withContext(Dispatchers.IO) {
        try {
            val response = api.getUnverifiedUsers()
            if (response.isSuccessful) {
                _unverifiedUsers.value = response.body()?.map { it.toUser() } ?: emptyList()
            }
        } catch (e: Exception) { /* keep cached */ }
    }

    suspend fun refreshAllUsers() = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllUsers()
            if (response.isSuccessful) {
                _allUsers.value = response.body()?.map { it.toUser() } ?: emptyList()
            }
        } catch (e: Exception) { /* keep cached */ }
    }

    suspend fun verifyUserNid(userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyUser(userId)
            if (response.isSuccessful) {
                refreshUnverifiedUsers()
                Result.success(Unit)
            } else Result.failure(Exception("Verification failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectUserNid(userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteUser(userId)
            if (response.isSuccessful) {
                refreshUnverifiedUsers()
                refreshAllUsers()
                Result.success(Unit)
            } else Result.failure(Exception("Reject failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Password Recovery ───────────────────────────────────────────────────────
    suspend fun submitRecoveryRequest(phone: String, nidNumber: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.submitRecoveryRequest(RecoveryRequestReq(phone, nidNumber))
            if (response.isSuccessful) Result.success(response.body()?.message ?: "Submitted")
            else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to submit request"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getRecoveryRequests(): Result<List<RecoveryRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRecoveryRequests()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(Exception("Failed to load recovery requests"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun approveRecoveryRequest(id: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.approveRecoveryRequest(id)
            if (response.isSuccessful) Result.success(response.body()?.message ?: "Approved")
            else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to approve"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectRecoveryRequest(id: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.rejectRecoveryRequest(id)
            if (response.isSuccessful) Result.success(response.body()?.message ?: "Rejected")
            else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to reject"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    // URI (content://) → temp File (Retrofit multipart জন্য)
    private fun uriToTempFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
