package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodDao {

    // --- Users Collection ---
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdDirect(id: Int): User?

    @Query("SELECT * FROM users WHERE blood_group = :bloodGroup AND availability = 1")
    fun getAvailableDonorsByBloodGroup(bloodGroup: String): Flow<List<User>>

    @Query("SELECT * FROM users WHERE availability = 1")
    fun getAllAvailableDonors(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE is_verified = 0")
    fun getUnverifiedUsers(): Flow<List<User>>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    // --- Blood Requests Collection ---
    @Query("SELECT * FROM blood_requests ORDER BY created_at DESC")
    fun getAllBloodRequests(): Flow<List<BloodRequest>>

    @Query("SELECT * FROM blood_requests WHERE status = 'active' ORDER BY created_at DESC")
    fun getActiveBloodRequests(): Flow<List<BloodRequest>>

    @Query("SELECT * FROM blood_requests WHERE id = :id LIMIT 1")
    fun getBloodRequestById(id: Int): Flow<BloodRequest?>

    @Query("SELECT * FROM blood_requests WHERE recipient_id = :recipientId ORDER BY created_at DESC")
    fun getRequestsByRecipientId(recipientId: Int): Flow<List<BloodRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodRequest(request: BloodRequest): Long

    @Update
    suspend fun updateBloodRequest(request: BloodRequest)

    // --- Donations (Response) Collection ---
    @Query("SELECT * FROM donations WHERE request_id = :requestId ORDER BY timestamp DESC")
    fun getDonationsByRequestId(requestId: Int): Flow<List<Donation>>

    @Query("SELECT * FROM donations WHERE donor_id = :donorId ORDER BY timestamp DESC")
    fun getDonationsByDonorId(donorId: Int): Flow<List<Donation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: Donation): Long

    @Update
    suspend fun updateDonation(donation: Donation)
}
