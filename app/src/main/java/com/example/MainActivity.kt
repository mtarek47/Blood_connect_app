package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.BloodDatabase
import com.example.data.repository.BloodRepository
import com.example.ui.AppNotification
import com.example.ui.BloodViewModel
import com.example.ui.Screen
import com.example.ui.ViewModelFactory
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RequestDetailsScreen
import com.example.ui.screens.RequestScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: BloodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room Initializers
        val database = BloodDatabase.getDatabase(applicationContext)
        val repository = BloodRepository(database.bloodDao)
        val factory = ViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[BloodViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainOrchestrator(viewModel)
            }
        }
    }
}

@Composable
fun MainOrchestrator(viewModel: BloodViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Real-Time Notification banner states
    var activePushNotification by remember { mutableStateOf<AppNotification?>(null) }
    var showPushBanner by remember { mutableStateOf(false) }

    // Listen to real-time incoming actions
    LaunchedEffect(Unit) {
        viewModel.newNotificationAlert.collect { alert ->
            activePushNotification = alert
            showPushBanner = true
            // Play a simulation toast alert represent FCM push
            Toast.makeText(context, "FCM Push Broadcast Received!", Toast.LENGTH_SHORT).show()
            delay(6000)
            showPushBanner = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val showBottomBar = currentScreen != Screen.Splash &&
                currentScreen != Screen.Login &&
                currentScreen != Screen.Register

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    AppBottomNavigationBar(
                        currentScreen = currentScreen,
                        onTabSelect = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Switchboard selector
                when (currentScreen) {
                    Screen.Splash -> SplashScreen(
                        onGetStarted = {
                            viewModel.navigateTo(if (currentUser != null) Screen.Dashboard else Screen.Login)
                        }
                    )
                    Screen.Login -> AuthScreen(
                        viewModel = viewModel,
                        isRegisterModeInitial = false
                    )
                    Screen.Register -> AuthScreen(
                        viewModel = viewModel,
                        isRegisterModeInitial = true
                    )
                    Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                    Screen.RequestCreate -> RequestScreen(viewModel = viewModel)
                    Screen.RequestDetails -> RequestDetailsScreen(viewModel = viewModel)
                    Screen.AdminMode -> AdminScreen(viewModel = viewModel)
                    Screen.Profile -> ProfileScreen(viewModel = viewModel)
                }
            }
        }

        // --- Simulated FCM Push Notification Drawer Dropdown Overlay ---
        AnimatedVisibility(
            visible = showPushBanner && activePushNotification != null,
            enter = slideInVertically(initialOffsetY = { -150 }),
            exit = slideOutVertically(targetOffsetY = { -150 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(14.dp)
        ) {
            val alert = activePushNotification!!
            Card(
                onClick = {
                    viewModel.selectRequest(alert.request)
                    showPushBanner = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_app_push_alert"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "VIEW",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: Screen,
    onTabSelect: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_bottom_nav_bar")
    ) {
        val homeActive = currentScreen == Screen.Dashboard || currentScreen == Screen.RequestDetails || currentScreen == Screen.AdminMode
        NavigationBarItem(
            selected = homeActive,
            onClick = { onTabSelect(Screen.Dashboard) },
            icon = {
                Icon(
                    imageVector = if (homeActive) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home Hub", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_tab_dashboard")
        )

        val requestActive = currentScreen == Screen.RequestCreate
        NavigationBarItem(
            selected = requestActive,
            onClick = { onTabSelect(Screen.RequestCreate) },
            icon = {
                Icon(
                    imageVector = if (requestActive) Icons.Default.PostAdd else Icons.Outlined.PostAdd,
                    contentDescription = "New Request"
                )
            },
            label = { Text("Request", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_tab_create")
        )

        val profileActive = currentScreen == Screen.Profile
        NavigationBarItem(
            selected = profileActive,
            onClick = { onTabSelect(Screen.Profile) },
            icon = {
                Icon(
                    imageVector = if (profileActive) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = "My Profile"
                )
            },
            label = { Text("My Profile", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}
