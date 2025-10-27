package com.lightalarm.nativeapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lightalarm.nativeapp.data.LightTheme
import com.lightalarm.nativeapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WakeUpPreviewScreen(
    theme: LightTheme = LightTheme.SUNRISE,
    onNavigateBack: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimating) 100f else 0f,
        animationSpec = tween(
            durationMillis = 20000, // 20 seconds
            easing = LinearEasing
        ),
        finishedListener = { isAnimating = false }
    )
    
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            progress = animatedProgress
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = WhiteText
                    )
                }
                Text(
                    text = "Wake Up Simulation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress Circle
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = ProgressBackground.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                
                // Progress circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ProgressFill.copy(alpha = 0.3f),
                                    ProgressFill
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Percentage text
                Text(
                    text = "${progress.toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Theme info
            Text(
                text = "${theme.displayName} • 20s duration",
                style = MaterialTheme.typography.bodyLarge,
                color = WhiteText
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start button
                Button(
                    onClick = {
                        isAnimating = true
                        progress = 0f
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(AccentBlue, AccentOrange)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = WhiteText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Wake Up",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteText
                            )
                        }
                    }
                }
                
                // Refresh button
                Button(
                    onClick = {
                        isAnimating = false
                        progress = 0f
                    },
                    modifier = Modifier.size(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhiteText.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = WhiteText
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Description
            Text(
                text = "Gradually brightens to wake you naturally",
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteText.copy(alpha = 0.8f)
            )
        }
    }
}
