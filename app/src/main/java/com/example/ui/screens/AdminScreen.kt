package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.ui.BloodViewModel
import com.example.ui.Screen

@Composable
fun AdminScreen(
    viewModel: BloodViewModel,
    modifier: Modifier = Modifier
) {
    val unverifiedUsers by viewModel.unverifiedUsers.collectAsState()
    val allRecs by viewModel.allUsers.collectAsState()
    val actionSuccess by viewModel.actionSuccess.collectAsState()

    var activeTab by remember { mutableStateOf("NID_PENDING") } // "NID_PENDING", "ALL_USERS", "REPORTS"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    modifier = Modifier.testTag("admin_back_btn")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Admin Hub Console",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Flash action notices
                AnimatedVisibility(visible = actionSuccess != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = actionSuccess ?: "",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Horizontal sub tabs selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AdminSubTab(
                        label = "NID Review (${unverifiedUsers.size})",
                        isSelected = activeTab == "NID_PENDING",
                        tag = "nid_tab",
                        onClick = { activeTab = "NID_PENDING" },
                        modifier = Modifier.weight(1f)
                    )
                    AdminSubTab(
                        label = "Donors DB (${allRecs.size})",
                        isSelected = activeTab == "ALL_USERS",
                        tag = "users_tab",
                        onClick = { activeTab = "ALL_USERS" },
                        modifier = Modifier.weight(1f)
                    )
                    AdminSubTab(
                        label = "Reports (0)",
                        isSelected = activeTab == "REPORTS",
                        tag = "reports_tab",
                        onClick = { activeTab = "REPORTS" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tab Content Render
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_tasks_list")
                ) {
                    if (activeTab == "NID_PENDING") {
                        if (unverifiedUsers.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(50.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "All accounts verified",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "There are no pending NID uploads in queue.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            items(unverifiedUsers) { user ->
                                AdminRequestCard(
                                    user = user,
                                    onVerifyClick = { viewModel.verifyUserNid(user.id) }
                                )
                            }
                        }
                    } else if (activeTab == "ALL_USERS") {
                        if (allRecs.isEmpty()) {
                            item {
                                Text(
                                    text = "Empty Database.",
                                    modifier = Modifier.padding(24.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(allRecs) { user ->
                                AdminUserCard(user = user)
                            }
                        }
                    } else {
                        // MOCK REPORTS CHANNEL FOR SYSTEM INTEGRITY
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Scam & Abuse Detection Active",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "We are monitoring spam requests. All active queries are matching clean coordinates. Zero reports filed today.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSubTab(
    label: String,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(tag),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AdminRequestCard(
    user: User,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("admin_review_user_${user.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── User Info Header ─────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile photo (small circle)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.profileImage.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(user.profileImage),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            user.bloodGroup,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user.name, fontWeight = FontWeight.Bold)
                    Text(
                        "Phone: ${user.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Address: ${user.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // ── NID Photos ──────────────────────────────────────────
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "NID Documents",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // NID Front
                NidImageBox(
                    uri = user.nidImageFront,
                    label = "NID • Front",
                    modifier = Modifier.weight(1f)
                )
                // NID Back
                NidImageBox(
                    uri = user.nidImageBack,
                    label = "NID • Back",
                    modifier = Modifier.weight(1f)
                )
            }
            // ──────────────────────────────────────────────────────

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onVerifyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_nid_btn_${user.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Approve & Mark Verified", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// NID ছবির জন্য reusable box — tap করলে fullscreen preview খুলবে
@Composable
fun NidImageBox(
    uri: String?,
    label: String,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }

    // Fullscreen preview dialog
    if (showPreview && !uri.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                // Full image
                AsyncImage(
                    model = Uri.parse(uri),
                    contentDescription = label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Label at bottom
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Close button (top-right)
                IconButton(
                    onClick = { showPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Thumbnail card
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (!uri.isNullOrBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                )
                .then(
                    if (!uri.isNullOrBlank()) Modifier.clickable { showPreview = true }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!uri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(uri),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Tap hint overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.55f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Tap to preview",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Not uploaded",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AdminUserCard(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(user.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, fontWeight = FontWeight.Bold)
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(text = user.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = if (user.availability) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (user.availability) "Online" else "Offline",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (user.availability) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
