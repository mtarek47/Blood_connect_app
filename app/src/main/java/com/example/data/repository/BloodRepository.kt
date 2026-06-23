package com.example.data.repository

import com.example.data.local.BloodDao
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BloodRepository(private val bloodDao: BloodDao) {

    val activeRequests: Flow<List<BloodRequest>> = bloodDao.getActiveBloodRequests()
    val allRequests: Flow<List<BloodRequest>> = bloodDao.getAllBloodRequests()
    val allAvailableDonors: Flow<List<User>> = bloodDao.getAllAvailableDonors()
    val unverifiedUsers: Flow<List<User>> = bloodDao.getUnverifiedUsers()
    val allUsers: Flow<List<User>> = bloodDao.getAllUsers()

    fun getAvailableDonors(bloodGroup: String): Flow<List<User>> {
        return if (bloodGroup == "All" || bloodGroup.isEmpty()) {
            bloodDao.getAllAvailableDonors()
        } else {
            bloodDao.getAvailableDonorsByBloodGroup(bloodGroup)
        }
    }

    fun getUserById(id: Int): Flow<User?> = bloodDao.getUserById(id)

    suspend fun getUserByIdDirect(id: Int): User? = withContext(Dispatchers.IO) {
        bloodDao.getUserByIdDirect(id)
    }

    suspend fun registerUser(user: User): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val existing = bloodDao.getUserByPhone(user.phone)
            if (existing != null) {
                Result.failure(Exception("Phone number already registered"))
            } else {
                val newId = bloodDao.insertUser(user)
                Result.success(newId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(phone: String, passwordHash: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = bloodDao.getUserByPhone(phone)
            if (user == null) {
                Result.failure(Exception("Phone number not found"))
            } else if (user.passwordHash != passwordHash) {
                Result.failure(Exception("Incorrect password"))
            } else {
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        bloodDao.updateUser(user)
    }

    suspend fun createBloodRequest(request: BloodRequest): Long = withContext(Dispatchers.IO) {
        bloodDao.insertBloodRequest(request)
    }

    suspend fun updateBloodRequest(request: BloodRequest) = withContext(Dispatchers.IO) {
        bloodDao.updateBloodRequest(request)
    }

    fun getRequestsByRecipient(recipientId: Int): Flow<List<BloodRequest>> {
        return bloodDao.getRequestsByRecipientId(recipientId)
    }

    fun getDonationsByRequest(requestId: Int): Flow<List<Donation>> {
        return bloodDao.getDonationsByRequestId(requestId)
    }

    fun getDonationsByDonor(donorId: Int): Flow<List<Donation>> {
        return bloodDao.getDonationsByDonorId(donorId)
    }

    suspend fun respondToRequest(donation: Donation): Long = withContext(Dispatchers.IO) {
        bloodDao.insertDonation(donation)
    }

    suspend fun updateDonation(donation: Donation) = withContext(Dispatchers.IO) {
        bloodDao.updateDonation(donation)
    }
}
