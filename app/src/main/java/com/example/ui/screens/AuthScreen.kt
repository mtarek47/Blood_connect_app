package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ui.BloodViewModel
import java.io.File

// Helper: camera-র জন্য temp file বানায় এবং URI return করে
private fun createImageUri(context: Context): Uri {
    val cacheDir = File(context.cacheDir, "camera_images").also { it.mkdirs() }
    val file = File.createTempFile("img_", ".jpg", cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun AuthScreen(
    viewModel: BloodViewModel,
    isRegisterModeInitial: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(isRegisterModeInitial) }
    val actionMessage by viewModel.actionMessage.collectAsState()

    // Forgot password states
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotPhone by remember { mutableStateOf("+880") }
    var forgotNid by remember { mutableStateOf("") }
    var forgotErrorMsg by remember { mutableStateOf<String?>(null) }
    var forgotSuccessMsg by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Form states
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+880") }
    var division by remember { mutableStateOf("") }
    var zilla by remember { mutableStateOf("") }
    var divisionDropdownExpanded by remember { mutableStateOf(false) }
    var zillaDropdownExpanded by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dobDay by remember { mutableStateOf("") }
    var dobMonth by remember { mutableStateOf("") }
    var dobYear by remember { mutableStateOf("") }

    // Real image URIs from camera
    var nidFrontUri by remember { mutableStateOf<Uri?>(null) }
    var nidBackUri by remember { mutableStateOf<Uri?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }

    // Temp URI holders (camera output লেখার আগে এখানে রাখা হয়)
    var pendingNidFrontUri by remember { mutableStateOf<Uri?>(null) }
    var pendingNidBackUri by remember { mutableStateOf<Uri?>(null) }
    var pendingProfileUri by remember { mutableStateOf<Uri?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var bloodDropdownExpanded by remember { mutableStateOf(false) }
    var genderDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val genders = listOf("Male", "Female")
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear - 70..currentYear - 16).map { it.toString() }.reversed()

    // ── Camera launchers ──────────────────────────────────────────────────────

    // NID Front camera launcher
    val nidFrontCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            nidFrontUri = pendingNidFrontUri
        }
    }

    // NID Back camera launcher
    val nidBackCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            nidBackUri = pendingNidBackUri
        }
    }

    // Profile photo camera launcher
    val profileCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profileUri = pendingProfileUri
        }
    }

    // ── Camera permission launcher ────────────────────────────────────────────
    // কোন button press হয়েছে তা track করার জন্য
    var pendingCameraAction by remember { mutableStateOf("") }
    
    // ── Gallery launchers ──────────────────────────────────────────────────────
    val nidFrontGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) nidFrontUri = uri
    }
    val nidBackGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) nidBackUri = uri
    }
    val profileGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) profileUri = uri
    }
    
    var imageSourceAction by remember { mutableStateOf("") }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission পেলে সেই action চালাও
            when (pendingCameraAction) {
                "nid_front" -> {
                    val uri = createImageUri(context)
                    pendingNidFrontUri = uri
                    nidFrontCameraLauncher.launch(uri)
                }
                "nid_back" -> {
                    val uri = createImageUri(context)
                    pendingNidBackUri = uri
                    nidBackCameraLauncher.launch(uri)
                }
                "profile" -> {
                    val uri = createImageUri(context)
                    pendingProfileUri = uri
                    profileCameraLauncher.launch(uri)
                }
            }
        }
    }

    // Helper: camera check করে launch করে
    fun launchCamera(action: String) {
        pendingCameraAction = action
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            when (action) {
                "nid_front" -> {
                    val uri = createImageUri(context)
                    pendingNidFrontUri = uri
                    nidFrontCameraLauncher.launch(uri)
                }
                "nid_back" -> {
                    val uri = createImageUri(context)
                    pendingNidBackUri = uri
                    nidBackCameraLauncher.launch(uri)
                }
                "profile" -> {
                    val uri = createImageUri(context)
                    pendingProfileUri = uri
                    profileCameraLauncher.launch(uri)
                }
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    if (showForgotDialog) {
        Dialog(onDismissRequest = {
            showForgotDialog = false
            forgotErrorMsg = null
            forgotSuccessMsg = null
        }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Recover Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (forgotErrorMsg != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = forgotErrorMsg ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (forgotSuccessMsg != null) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = forgotSuccessMsg ?: "",
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = forgotPhone,
                        onValueChange = { forgotPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                        value = forgotNid,
                        onValueChange = { forgotNid = it },
                        label = { Text("NID Number") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Button(
                        onClick = {
                            forgotErrorMsg = null
                            forgotSuccessMsg = null
                            coroutineScope.launch {
                                val result = viewModel.submitRecoveryRequest(forgotPhone, forgotNid)
                                result.onSuccess {
                                    forgotSuccessMsg = "Your password recovery request has been submitted successfully. An admin will review your request within 24 hours. Once approved, your password will be reset to the default: 1234."
                                    kotlinx.coroutines.delay(5000)
                                    showForgotDialog = false
                                }.onFailure { err ->
                                    forgotErrorMsg = err.message ?: "User not found or request failed."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Submit Request")
                    }
                    TextButton(onClick = {
                        showForgotDialog = false
                        forgotErrorMsg = null
                        forgotSuccessMsg = null
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isRegisterMode) {
                if (imageSourceAction.isNotEmpty()) {
                    Dialog(onDismissRequest = { imageSourceAction = "" }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Select Image Source",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Camera Option
                                    Card(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clickable {
                                                val action = imageSourceAction
                                                imageSourceAction = ""
                                                launchCamera(action)
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = "Camera",
                                                modifier = Modifier.size(40.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Camera",
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    
                                    // Gallery Option
                                    Card(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clickable {
                                                val action = imageSourceAction
                                                imageSourceAction = ""
                                                when (action) {
                                                    "nid_front" -> nidFrontGalleryLauncher.launch("image/*")
                                                    "nid_back" -> nidBackGalleryLauncher.launch("image/*")
                                                    "profile" -> profileGalleryLauncher.launch("image/*")
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = "Gallery",
                                                modifier = Modifier.size(40.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Gallery",
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isRegisterMode = false },
                        modifier = Modifier.testTag("back_to_login_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Create Donor Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (!isRegisterMode) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "❤",
                        fontSize = 65.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Welcome to Blood Connect",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sign in to query matched donors or place request",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                // Error Banner
                AnimatedVisibility(visible = actionMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (actionMessage?.isError == true) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = actionMessage?.message ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (actionMessage?.isError == true) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF2E7D32),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Crossfade(targetState = isRegisterMode, label = "auth_form") { isReg ->
                    if (isReg) {
                        // REGISTRATION FORM
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { 
                                    if (it.startsWith("+880") && it.length <= 14) {
                                        val remaining = it.removePrefix("+880")
                                        if (remaining.all { char -> char.isDigit() }) {
                                            phone = it
                                        }
                                    } else if (it.length < 4) {
                                        phone = "+880"
                                    }
                                },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_phone_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Division Dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = division.ifEmpty { "Select Division" },
                                    label = { Text("Division") },
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { divisionDropdownExpanded = true }
                                        .testTag("reg_division_dropdown"),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { divisionDropdownExpanded = true }
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                DropdownMenu(
                                    expanded = divisionDropdownExpanded,
                                    onDismissRequest = { divisionDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    com.example.data.LocationData.divisions.forEach { div ->
                                        DropdownMenuItem(
                                            text = { Text(div) },
                                            onClick = {
                                                division = div
                                                zilla = "" // Reset zilla when division changes
                                                divisionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Zilla Dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = zilla.ifEmpty { "Select Zilla" },
                                    label = { Text("Zilla / District") },
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { if (division.isNotEmpty()) zillaDropdownExpanded = true }
                                        .testTag("reg_zilla_dropdown"),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { if (division.isNotEmpty()) zillaDropdownExpanded = true }
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                DropdownMenu(
                                    expanded = zillaDropdownExpanded,
                                    onDismissRequest = { zillaDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    val zillas = com.example.data.LocationData.divisionsAndZillas[division] ?: emptyList()
                                    zillas.forEach { zil ->
                                        DropdownMenuItem(
                                            text = { Text(zil) },
                                            onClick = {
                                                zilla = zil
                                                zillaDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Blood Group Selector dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = bloodGroup.ifEmpty { "Select Blood Group" },
                                    label = { Text("Blood Group") },
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { bloodDropdownExpanded = true }
                                        .testTag("reg_blood_group_dropdown"),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { bloodDropdownExpanded = true }
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                DropdownMenu(
                                    expanded = bloodDropdownExpanded,
                                    onDismissRequest = { bloodDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    bloodGroups.forEach { group ->
                                        DropdownMenuItem(
                                            text = { Text(group, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                bloodGroup = group
                                                bloodDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("blood_group_item_$group")
                                        )
                                    }
                                }
                            }

                            // Gender Selector dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = gender.ifEmpty { "Select Gender" },
                                    label = { Text("Gender") },
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { genderDropdownExpanded = true },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { genderDropdownExpanded = true }
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                DropdownMenu(
                                    expanded = genderDropdownExpanded,
                                    onDismissRequest = { genderDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    genders.forEach { g ->
                                        DropdownMenuItem(
                                            text = { Text(g) },
                                            onClick = {
                                                gender = g
                                                genderDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Date of Birth Row
                            Text("Date of Birth", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Day
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = dobDay.ifEmpty { "Day" },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { dayDropdownExpanded = true },
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { dayDropdownExpanded = true }) },
                                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline),
                                        enabled = false,
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    DropdownMenu(expanded = dayDropdownExpanded, onDismissRequest = { dayDropdownExpanded = false }) {
                                        days.forEach { d ->
                                            DropdownMenuItem(text = { Text(d) }, onClick = { dobDay = d; dayDropdownExpanded = false })
                                        }
                                    }
                                }
                                // Month
                                Box(modifier = Modifier.weight(1.5f)) {
                                    OutlinedTextField(
                                        value = dobMonth.ifEmpty { "Month" },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { monthDropdownExpanded = true },
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { monthDropdownExpanded = true }) },
                                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline),
                                        enabled = false,
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    DropdownMenu(expanded = monthDropdownExpanded, onDismissRequest = { monthDropdownExpanded = false }) {
                                        months.forEach { m ->
                                            DropdownMenuItem(text = { Text(m) }, onClick = { dobMonth = m; monthDropdownExpanded = false })
                                        }
                                    }
                                }
                                // Year
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = dobYear.ifEmpty { "Year" },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { yearDropdownExpanded = true },
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { yearDropdownExpanded = true }) },
                                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline),
                                        enabled = false,
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    DropdownMenu(expanded = yearDropdownExpanded, onDismissRequest = { yearDropdownExpanded = false }) {
                                        years.forEach { y ->
                                            DropdownMenuItem(text = { Text(y) }, onClick = { dobYear = y; yearDropdownExpanded = false })
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_password_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // ── Document / Photo Section ──────────────────────────────────────
                            Text(
                                text = "NID & Profile Verification Documents",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Front NID — camera launch
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { imageSourceAction = "nid_front" }
                                        .testTag("attach_nid_front_btn"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (nidFrontUri != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (nidFrontUri != null) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = if (nidFrontUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (nidFrontUri != null) "Front NID Attached" else "Attach NID Front",
                                            style = MaterialTheme.typography.labelMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Back NID — camera launch
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { imageSourceAction = "nid_back" }
                                        .testTag("attach_nid_back_btn"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (nidBackUri != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (nidBackUri != null) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = if (nidBackUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (nidBackUri != null) "Back NID Attached" else "Attach NID Back",
                                            style = MaterialTheme.typography.labelMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Profile photo — camera launch
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { imageSourceAction = "profile" }
                                    .testTag("attach_profile_btn"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (profileUri != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (profileUri != null) Icons.Default.CheckCircle else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (profileUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (profileUri != null) "Profile Image Configured" else "Attach Profile Picture",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            // ─────────────────────────────────────────────────────────────────

                            Button(
                                onClick = {
                                    val fullAddress = if (division.isNotBlank() && zilla.isNotBlank()) "$zilla, $division" else ""
                                    val fullDob = if (dobDay.isNotBlank() && dobMonth.isNotBlank() && dobYear.isNotBlank()) "$dobDay $dobMonth $dobYear" else ""
                                    viewModel.register(
                                        name = name,
                                        phone = phone,
                                        address = fullAddress,
                                        bloodGroup = bloodGroup,
                                        gender = gender,
                                        dob = fullDob,
                                        password = password,
                                        nidFront = nidFrontUri?.toString(),
                                        nidBack = nidBackUri?.toString(),
                                        profilePhoto = profileUri?.toString()
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(top = 8.dp)
                                    .testTag("submit_register_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Register & Verify Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Already have a profile?")
                                TextButton(
                                    onClick = { isRegisterMode = false },
                                    modifier = Modifier.testTag("go_to_login_btn")
                                ) {
                                    Text("Sign In", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // LOGIN FORM
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { 
                                    if (it.startsWith("+880") && it.length <= 14) {
                                        val remaining = it.removePrefix("+880")
                                        if (remaining.all { char -> char.isDigit() }) {
                                            phone = it
                                        }
                                    } else if (it.length < 4) {
                                        phone = "+880"
                                    }
                                },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_phone_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.login(phone, password)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(top = 10.dp)
                                    .testTag("submit_login_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            TextButton(
                                onClick = { showForgotDialog = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                            ) {
                                Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("New to Blood Connect?")
                                TextButton(
                                    onClick = { isRegisterMode = true },
                                    modifier = Modifier.testTag("go_to_register_btn")
                                ) {
                                    Text("Register Now", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Developer details
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Blood Connect App ",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                    Text(
                                        text =  "Version 1.0.0 \n" +
                                                "Community & Non-Profit Focused.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Row {
                                        Text(
                                            text = "Developer details : ",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "https://tarekparvez.dev/",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                            ),
                                            modifier = Modifier.clickable {
                                                uriHandler.openUri("https://tarekparvez.dev/")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
