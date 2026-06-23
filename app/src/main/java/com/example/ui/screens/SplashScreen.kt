package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishPrimary

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(1000)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Vector Art using Compose Canvas (Blood Drop & Heart outline)
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .testTag("app_logo_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val width = size.width
                            val height = size.height

                            // Draw a beautiful crimson blood drop
                            val dropPath = Path().apply {
                                moveTo(width / 2f, 5f)
                                cubicTo(
                                    width * 0.9f, height * 0.5f,
                                    width * 0.95f, height * 0.95f,
                                    width / 2f, height
                                )
                                cubicTo(
                                    width * 0.05f, height * 0.95f,
                                    width * 0.10f, height * 0.5f,
                                    width / 2f, 5f
                                )
                                close()
                            }

                            drawPath(
                                path = dropPath,
                                color = PolishPrimary
                            )

                            // Inner heart cross glowing effect
                            drawCircle(
                                color = Color.White.copy(alpha = 0.25f),
                                radius = 25f,
                                center = Offset(width / 2f, height * 0.7f)
                            )

                            // Clean medical cross in center
                            val crossHalfLength = 12f
                            val crossThickness = 6f
                            val centerY = height * 0.7f
                            val centerX = width / 2f

                            // Horizontal
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(centerX - crossHalfLength, centerY - crossThickness / 2f),
                                size = androidx.compose.ui.geometry.Size(crossHalfLength * 2, crossThickness)
                            )
                            // Vertical
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(centerX - crossThickness / 2f, centerY - crossHalfLength),
                                size = androidx.compose.ui.geometry.Size(crossThickness, crossHalfLength * 2)
                            )
                        }

                        // Echo pulses around the drop
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = PolishPrimary.copy(alpha = 0.15f),
                                radius = size.minDimension / 2.2f,
                                style = Stroke(width = 4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Blood Connect",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("app_title")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Real-time life saving donor network.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Minimal onboarding highlights card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureRow(
                            emoji = "⚡",
                            title = "Smart Matching Engine",
                            description = "Find matching active donors instantly based on requested blood type."
                        )
                        FeatureRow(
                            emoji = "🔔",
                            title = "Instant Alert Notifications",
                            description = "Matching donors receive a push notification details screen in real-time."
                        )
                        FeatureRow(
                            emoji = "🛡️",
                            title = "Secure Identity Verification",
                            description = "National NID verification layer ensures safety and legitimacy of users."
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(52.dp)
                            .testTag("get_started_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureRow(emoji: String, title: String, description: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
