package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AdminConfig
import com.example.data.local.BloodDatabase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // UI Feedback status (Errors/Success)
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    // Simulating Live In-App Push Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Shared flow to trigger alert banners
    private val _newNotificationAlert = MutableSharedFlow<AppNotification>()
    val newNotificationAlert: SharedFlow<AppNotification> = _newNotificationAlert.asSharedFlow()

    // Observed fields
    val activeRequests: StateFlow<List<BloodRequest>> = repository.activeRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unverifiedUsers: StateFlow<List<User>> = repository.unverifiedUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Donors filter
    val searchedDonors: StateFlow<List<User>> = _searchedBloodGroup
        .flatMapLatest { group -> repository.getAvailableDonors(group) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map of request responses
    val currentRequestResponses: StateFlow<List<Donation>> = _selectedRequest
        .flatMapLatest { req ->
            if (req != null) repository.getDonationsByRequest(req.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedInitialMockData()
    }

    private fun seedInitialMockData() = viewModelScope.launch {
        val users = repository.allUsers.first()
        if (users.isEmpty()) {
            // শুধু admin user seed করা হচ্ছে
            repository.registerUser(
                User(
                    name = "Admin",
                    phone = AdminConfig.ADMIN_PHONE,
                    address = "Blood Connect HQ",
                    bloodGroup = "O+",
                    passwordHash = AdminConfig.ADMIN_PASSWORD,
                    isVerified = true,
                    isAdmin = true,
                    availability = false
                )
            )
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _authError.value = null
        _actionSuccess.value = null
    }

    fun selectRequest(request: BloodRequest) {
        _selectedRequest.value = request
        navigateTo(Screen.RequestDetails)
    }

    fun setSearchedBloodGroup(group: String) {
        _searchedBloodGroup.value = group
    }

    // Auth actions
    fun register(
        name: String,
        phone: String,
        address: String,
        bloodGroup: String,
        password: String,
        nidFront: String?,
        nidBack: String?,
        profilePhoto: String?
    ) = viewModelScope.launch {
        _authError.value = null
        if (name.isBlank() || phone.isBlank() || address.isBlank() || password.isBlank()) {
            _authError.value = "All key fields must be completed!"
            return@launch
        }
        val newUser = User(
            name = name,
            phone = phone,
            address = address,
            bloodGroup = bloodGroup,
            passwordHash = password, // Local mock hash
            nidImageFront = nidFront,
            nidImageBack = nidBack,
            profileImage = profilePhoto,
            availability = true,
            isVerified = false // Needs admin check
        )
        val result = repository.registerUser(newUser)
        result.onSuccess { id ->
            val registered = newUser.copy(id = id.toInt())
            _currentUser.value = registered
            _actionSuccess.value = "Registration completely successful! NID pending verification."
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
        val result = repository.loginUser(phone, password)
        result.onSuccess { user ->
            _currentUser.value = user
            _actionSuccess.value = "Welcome back, ${user.name}!"
            navigateTo(Screen.Dashboard)
        }.onFailure { err ->
            _authError.value = err.message ?: "Authentication failed."
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedRequest.value = null
        _notifications.value = emptyList()
        navigateTo(Screen.Login)
    }

    fun toggleAvailability() = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val updated = user.copy(availability = !user.availability)
        repository.updateUser(updated)
        _currentUser.value = updated
        _actionSuccess.value = "Availability updated successfully!"
    }

    fun updateAddress(newAddress: String) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        if (newAddress.isBlank()) return@launch
        val updated = user.copy(address = newAddress)
        repository.updateUser(updated)
        _currentUser.value = updated
        _actionSuccess.value = "Address database updated!"
    }

    // Post new blood request
    fun createRequest(
        bloodGroup: String,
        location: String,
        hospitalName: String?,
        urgencyLevel: String
    ) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        if (location.isBlank()) {
            _authError.value = "Please enter request location"
            return@launch
        }

        val request = BloodRequest(
            recipientId = user.id,
            recipientName = user.name,
            recipientPhone = user.phone,
            bloodGroup = bloodGroup,
            location = location,
            hospitalName = hospitalName,
            urgencyLevel = urgencyLevel,
            status = "active"
        )
        val reqId = repository.createBloodRequest(request)

        // Trigger dynamic mock notification to matching users
        triggerMatchingNotification(request.copy(id = reqId.toInt()))

        _actionSuccess.value = "Emergency request published! Matching donors notified."
        navigateTo(Screen.Dashboard)
    }

    fun completeRequest(request: BloodRequest) = viewModelScope.launch {
        val updated = request.copy(status = "completed")
        repository.updateBloodRequest(updated)
        if (_selectedRequest.value?.id == request.id) {
            _selectedRequest.value = updated
        }
        _actionSuccess.value = "Request marked as completed!"
    }

    // Respond to Request (Accept / Reject)
    fun respondToRequest(request: BloodRequest, isAccepted: Boolean) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        if (isAccepted) {
            val donation = Donation(
                donorId = user.id,
                donorName = user.name,
                donorPhone = user.phone,
                donorProfileImage = user.profileImage,
                requestId = request.id,
                status = "accepted"
            )
            repository.respondToRequest(donation)
            _actionSuccess.value = "Thank you! Contact details shared with ${request.recipientName}."
        } else {
            _actionSuccess.value = "Request ignored."
        }
    }

    // Admin verify user NID
    fun verifyUserNid(userId: Int) = viewModelScope.launch {
        val user = repository.getUserByIdDirect(userId)
        if (user != null) {
            val updated = user.copy(isVerified = true)
            repository.updateUser(updated)
            // If the current user is verified, update session state too
            if (_currentUser.value?.id == userId) {
                _currentUser.value = updated
            }
            _actionSuccess.value = "NID documents verified for ${user.name}!"
        }
    }

    // Simulate FCM background push notification
    private fun triggerMatchingNotification(request: BloodRequest) {
        viewModelScope.launch {
            // Find matched donor criteria: checks matching blood type and if user is available
            val potentialDonors = repository.getAvailableDonors(request.bloodGroup).first()

            // In our dynamic local test, if our currentUser or mock profiles meet the criteria,
            // we simulate delivering a prompt.
            val notification = AppNotification(
                id = request.id,
                title = "EMERGENCY - ${request.bloodGroup} Blood Required!",
                message = "${request.recipientName} needs blood at ${request.location}. Urgency: ${request.urgencyLevel}.",
                bloodGroup = request.bloodGroup,
                request = request
            )

            // Add to simulated notifications center
            val currentList = _notifications.value.toMutableList()
            currentList.add(0, notification)
            _notifications.value = currentList

            // Trigger the live Alert Bar
            _newNotificationAlert.emit(notification)
        }
    }

    fun dismissNotification(id: Int) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }
}

// Sealed Screens representation
sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Register : Screen()
    object Dashboard : Screen()
    object RequestCreate : Screen()
    object RequestDetails : Screen()
    object AdminMode : Screen()
    object Profile : Screen()
}

// Simulated App push notification structure
data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val bloodGroup: String,
    val request: BloodRequest,
    val timestamp: Long = System.currentTimeMillis()
)

// Factory
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
