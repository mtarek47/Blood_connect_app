package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.User
import com.example.data.repository.BloodRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive

class BloodViewModel(
    application: Application,
    private val repository: BloodRepository
) : AndroidViewModel(application) {

    // Current logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Computed: logged-in user ki admin?
    val isAdminUser: StateFlow<Boolean> = _currentUser
        .combine(_currentUser) { user, _ -> user?.isAdmin == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Screen State / Navigation
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Selected Blood Request for detailing
    private val _selectedRequest = MutableStateFlow<BloodRequest?>(null)
    val selectedRequest: StateFlow<BloodRequest?> = _selectedRequest.asStateFlow()

    // Filter Blood Group for Search
    private val _searchedBloodGroup = MutableStateFlow("All")
    val searchedBloodGroup: StateFlow<String> = _searchedBloodGroup.asStateFlow()

    // UI Feedback
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // In-app notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _newNotificationAlert = MutableSharedFlow<AppNotification>()
    val newNotificationAlert: SharedFlow<AppNotification> = _newNotificationAlert.asSharedFlow()

    // Observed from repository flows
    val activeRequests: StateFlow<List<BloodRequest>> = repository.activeRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unverifiedUsers: StateFlow<List<User>> = repository.unverifiedUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Donors list (refreshed on blood group change)
    private val _searchedDonors = MutableStateFlow<List<User>>(emptyList())
    val searchedDonors: StateFlow<List<User>> = _searchedDonors.asStateFlow()

    // Donations for selected request
    private val _currentRequestResponses = MutableStateFlow<List<Donation>>(emptyList())
    val currentRequestResponses: StateFlow<List<Donation>> = _currentRequestResponses.asStateFlow()

    init {
        checkExistingSession()
        startRealTimePolling()
    }

    // Auto-login if token exists
    private fun checkExistingSession() = viewModelScope.launch {
        _isLoading.value = true
        val token = repository.sessionManager.loadToken()
        if (token != null) {
            val userResult = repository.getCurrentUser()
            if (userResult.isSuccess) {
                _currentUser.value = userResult.getOrNull()
                navigateTo(Screen.Dashboard)
                refreshAll()
                if (_currentUser.value?.isAdmin == true) {
                    repository.refreshUnverifiedUsers()
                    repository.refreshAllUsers()
                }
            } else {
                repository.logout()
                _currentUser.value = null
                navigateTo(Screen.Login)
            }
        } else {
            navigateTo(Screen.Login)
        }
        _isLoading.value = false
    }

    private fun startRealTimePolling() = viewModelScope.launch {
        // Poll every 5 seconds
        launch {
            while (isActive) {
                delay(5000)
                if (_currentUser.value != null && 
                    currentScreen.value != Screen.Splash && 
                    currentScreen.value != Screen.Login && 
                    currentScreen.value != Screen.Register) {
                    repository.refreshActiveRequests()
                    refreshDonors(_searchedBloodGroup.value)
                }
            }
        }
        
        // Listen for changes and notify
        var firstLoad = true
        var oldRequests = emptyList<BloodRequest>()
        activeRequests.collect { newRequests ->
            if (firstLoad) {
                firstLoad = false
                oldRequests = newRequests
                return@collect
            }
            
            // Only trigger if we already had some data or have loaded for the first time
            if (oldRequests.isNotEmpty()) {
                val newIds = newRequests.map { it.id }.toSet() - oldRequests.map { it.id }.toSet()
                newIds.forEach { newId ->
                    val newReq = newRequests.find { it.id == newId }
                    if (newReq != null && newReq.recipientId != _currentUser.value?.id) {
                        val notification = AppNotification(
                            id = System.currentTimeMillis().toInt(),
                            title = "EMERGENCY - ${newReq.bloodGroup} Blood Required!",
                            message = "${newReq.recipientName} needs blood at ${newReq.location}. Urgency: ${newReq.urgencyLevel}.",
                            bloodGroup = newReq.bloodGroup,
                            request = newReq
                        )
                        val list = _notifications.value.toMutableList()
                        list.add(0, notification)
                        _notifications.value = list
                        _newNotificationAlert.emit(notification)
                    }
                }
            }
            oldRequests = newRequests
        }
    }

    private fun refreshAll() = viewModelScope.launch {
        repository.refreshActiveRequests()
        refreshDonors(_searchedBloodGroup.value)
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _authError.value = null
        _actionSuccess.value = null
    }

    fun selectRequest(request: BloodRequest) {
        _selectedRequest.value = request
        navigateTo(Screen.RequestDetails)
        loadDonationsForRequest(request.id)
    }

    private fun loadDonationsForRequest(requestId: Int) = viewModelScope.launch {
        _currentRequestResponses.value = repository.getDonationsForRequest(requestId)
    }

    fun setSearchedBloodGroup(group: String) {
        _searchedBloodGroup.value = group
        viewModelScope.launch { refreshDonors(group) }
    }

    private suspend fun refreshDonors(bloodGroup: String) {
        val donors = repository.refreshDonors(bloodGroup)
        val currentUserId = currentUser.value?.id
        _searchedDonors.value = if (currentUserId != null) {
            donors.filter { it.id != currentUserId }
        } else {
            donors
        }
    }

    // ─── Auth ──────────────────────────────────────────────────────────────────
    fun register(
        name: String, phone: String, address: String,
        bloodGroup: String, gender: String, dob: String, password: String,
        nidFront: String?, nidBack: String?, profilePhoto: String?
    ) = viewModelScope.launch {
        _authError.value = null
        if (name.isBlank() || phone.isBlank() || address.isBlank() || password.isBlank() || bloodGroup.isBlank() || gender.isBlank() || dob.isBlank()) {
            _authError.value = "All key fields including blood group, gender, and date of birth must be completed!"
            return@launch
        }
        if (phone.length != 14 || !phone.startsWith("+880")) {
            _authError.value = "Phone number must start with +880 and contain 10 digits."
            return@launch
        }
        if (nidFront.isNullOrBlank() || nidBack.isNullOrBlank() || profilePhoto.isNullOrBlank()) {
            _authError.value = "You must provide your NID (Front & Back) and a Profile Picture!"
            return@launch
        }
        _isLoading.value = true
        val result = repository.registerUser(
            name, phone, address, bloodGroup, gender, dob, password,
            profilePhoto, nidFront, nidBack
        )
        _isLoading.value = false
        result.onSuccess { user ->
            _currentUser.value = user
            _actionSuccess.value = "Registration successful! NID pending verification."
            refreshAll()
            navigateTo(Screen.Dashboard)
        }.onFailure { err ->
            _authError.value = err.message ?: "Registration failed."
        }
    }

    fun login(phone: String, password: String) = viewModelScope.launch {
        _authError.value = null
        if (phone.isBlank() || password.isBlank()) {
            _authError.value = "Please complete phone and password fields."
            return@launch
        }
        if (phone.length != 14 || !phone.startsWith("+880")) {
            _authError.value = "Phone number must start with +880 and contain 10 digits."
            return@launch
        }
        _isLoading.value = true
        val result = repository.loginUser(phone, password)
        _isLoading.value = false
        result.onSuccess { user ->
            _currentUser.value = user
            _actionSuccess.value = "Welcome back, ${user.name}!"
            refreshAll()
            if (user.isAdmin) {
                repository.refreshUnverifiedUsers()
                repository.refreshAllUsers()
            }
            navigateTo(Screen.Dashboard)
        }.onFailure { err ->
            _authError.value = err.message ?: "Authentication failed."
        }
    }

    fun logout() = viewModelScope.launch {
        _isLoading.value = true
        kotlinx.coroutines.delay(800) // Modern loading feel
        repository.logout()
        _currentUser.value = null
        _selectedRequest.value = null
        _notifications.value = emptyList()
        navigateTo(Screen.Login)
        _isLoading.value = false
    }

    // ─── Profile ───────────────────────────────────────────────────────────────
    fun toggleAvailability() = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        if (!user.isVerified) {
            _authError.value = "Your ID is not verified yet. Please wait for admin approval to become an active donor."
            return@launch
        }
        val newAvailability = !user.availability
        val result = repository.updateUser(availability = newAvailability)
        result.onSuccess {
            _currentUser.value = user.copy(availability = newAvailability)
            _actionSuccess.value = "Availability updated successfully!"
        }
    }

    fun updateAddress(newAddress: String) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        if (newAddress.isBlank()) return@launch
        val result = repository.updateUser(address = newAddress)
        result.onSuccess {
            _currentUser.value = user.copy(address = newAddress)
            _actionSuccess.value = "Address updated!"
        }
    }

    // ─── Blood Requests ────────────────────────────────────────────────────────
    fun createRequest(
        bloodGroup: String, gender: String, age: String, location: String,
        hospitalName: String, urgencyLevel: String
    ) = viewModelScope.launch {
        if (bloodGroup.isBlank() || location.isBlank() || urgencyLevel.isBlank() || gender.isBlank() || age.isBlank()) {
            _actionSuccess.value = "Please fill in all required fields (Blood Group, Gender, Age, Location, Urgency)"
            return@launch
        }
        val user = _currentUser.value ?: return@launch
        if (!user.isVerified) {
            _authError.value = "Your ID is not verified yet. Please wait for admin approval to request blood."
            return@launch
        }
        _isLoading.value = true
        val result = repository.createBloodRequest(bloodGroup, gender, age, location, hospitalName, urgencyLevel)
        _isLoading.value = false
        result.onSuccess {
            // In-app notification
            val fakeReq = BloodRequest(
                recipientId = user.id, recipientName = user.name,
                recipientPhone = user.phone, bloodGroup = bloodGroup,
                gender = gender, age = age,
                location = location, hospitalName = hospitalName,
                urgencyLevel = urgencyLevel, status = "active"
            )
            val notification = AppNotification(
                id = System.currentTimeMillis().toInt(),
                title = "EMERGENCY - $bloodGroup Blood Required!",
                message = "${user.name} needs blood at $location. Urgency: $urgencyLevel.",
                bloodGroup = bloodGroup,
                request = fakeReq
            )
            val list = _notifications.value.toMutableList()
            list.add(0, notification)
            _notifications.value = list
            _newNotificationAlert.emit(notification)

            _actionSuccess.value = "Emergency request published!"
            navigateTo(Screen.Dashboard)
        }.onFailure {
            _authError.value = "Failed to create request. Check your connection."
        }
    }

    fun completeRequest(request: BloodRequest) = viewModelScope.launch {
        val result = repository.completeRequest(request.id)
        result.onSuccess {
            _actionSuccess.value = "Request marked as completed!"
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.changePassword(oldPass, newPass)
            _isLoading.value = false
            if (result.isSuccess) {
                _actionSuccess.value = "Password changed successfully"
                delay(3000)
                _actionSuccess.value = null
            } else {
                _actionSuccess.value = "Failed to change password"
                delay(3000)
                _actionSuccess.value = null
            }
        }
    }

    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateProfileImage(uri.toString())
            if (result.isSuccess) {
                // Refresh the current user to get the new profile picture URL
                repository.getCurrentUser().onSuccess { user ->
                    _currentUser.value = user
                    _actionSuccess.value = "Profile picture updated!"
                }
            } else {
                _actionSuccess.value = "Failed to update profile picture"
            }
            _isLoading.value = false
            delay(3000)
            _actionSuccess.value = null
        }
    }

    // ─── ADMIN Panel ─────────────────────────────────────────────────────────────
    // ─── Donations ─────────────────────────────────────────────────────────────
    fun respondToRequest(request: BloodRequest, isAccepted: Boolean) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        if (!user.isVerified && isAccepted) {
            _authError.value = "Your ID is not verified yet. Please wait for admin approval to donate blood."
            return@launch
        }
        
        if (isAccepted) {
            val result = repository.respondToRequest(request.id)
            result.onSuccess {
                _actionSuccess.value = "Thank you! Contact details shared with ${request.recipientName}."
            }.onFailure {
                _authError.value = "Could not respond. Try again."
            }
        } else {
            _actionSuccess.value = "Request ignored."
        }
    }

    // ─── Admin ─────────────────────────────────────────────────────────────────
    fun verifyUserNid(userId: Int) = viewModelScope.launch {
        val result = repository.verifyUserNid(userId)
        result.onSuccess {
            _actionSuccess.value = "NID verified successfully!"
        }.onFailure {
            _authError.value = "Verification failed. Try again."
        }
    }

    fun rejectUserNid(userId: Int) = viewModelScope.launch {
        val result = repository.rejectUserNid(userId)
        result.onSuccess {
            _actionSuccess.value = "User deleted successfully."
        }.onFailure {
            _authError.value = "Deletion failed. Try again."
        }
    }

    fun loadAdminData() = viewModelScope.launch {
        repository.refreshUnverifiedUsers()
        repository.refreshAllUsers()
    }

    fun dismissNotification(id: Int) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }
}

// ─── Sealed Screens ────────────────────────────────────────────────────────────
sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Register : Screen()
    object Dashboard : Screen()
    object RequestCreate : Screen()
    object RequestDetails : Screen()
    object AdminMode : Screen()
    object Profile : Screen()
    object Notifications : Screen()
}

// ─── In-app notification ───────────────────────────────────────────────────────
data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val bloodGroup: String,
    val request: BloodRequest,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── Factory ──────────────────────────────────────────────────────────────────
class ViewModelFactory(
    private val application: Application,
    private val repository: BloodRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloodViewModel::class.java)) {
            return BloodViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
