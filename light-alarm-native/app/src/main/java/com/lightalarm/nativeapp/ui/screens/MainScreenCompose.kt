package com.lightalarm.nativeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lightalarm.nativeapp.ui.theme.*
import com.lightalarm.nativeapp.viewmodel.AlarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenCompose(
    onNavigateToAddAlarm: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPreview: () -> Unit,
    viewModel: AlarmViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsState()

    // Mode toggle state
    var currentMode by remember { mutableStateOf(AlarmMode.ALARM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Mode Toggle
        ModeToggle(
            currentMode = currentMode,
            onModeChange = { currentMode = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Content
        when (currentMode) {
            AlarmMode.ALARM -> AlarmModeContent(
                    alarms = alarms,
                onAddAlarm = onNavigateToAddAlarm,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToPreview = onNavigateToPreview
            )
            AlarmMode.SLEEP -> SleepModeContent()
        }
    }
}

enum class AlarmMode {
    ALARM, SLEEP
}

@Composable
private fun ModeToggle(
    currentMode: AlarmMode,
    onModeChange: (AlarmMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ToggleButton(
            text = "Alarm",
            icon = Icons.Default.Alarm,
            isActive = currentMode == AlarmMode.ALARM,
            onClick = { onModeChange(AlarmMode.ALARM) }
        )

        Spacer(modifier = Modifier.width(16.dp))

        ToggleButton(
            text = "Sleep",
            icon = Icons.Default.Bedtime,
            isActive = currentMode == AlarmMode.SLEEP,
            onClick = { onModeChange(AlarmMode.SLEEP) }
        )
    }
}

@Composable
private fun ToggleButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) DarkToggleActive else DarkToggleInactive
    val textColor = if (isActive) Color.White else Color.Gray

    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AlarmModeContent(
    alarms: List<com.lightalarm.nativeapp.data.AlarmConfig>,
    onAddAlarm: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "LightAlarm",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wake up gently",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        // Add Alarm Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAddAlarm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkAccent
                )
                ) {
                    Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Alarm")
                }
            }
            
        // Alarms List
            if (alarms.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            items(alarms) { alarm ->
                AlarmItemCard(alarm = alarm)
            }
        }

        // Preview Button (only show if there are alarms)
        if (alarms.isNotEmpty()) {
            item {
                    Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToPreview,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkAccent
                    )
                ) {
                    Text(
                        text = "Preview Wake Up",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SleepModeContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = "Sleep Mode",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
            Text(
            text = "Sleep Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
            Text(
            text = "Sleep tracking and insights coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = "No Alarms",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
                    Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No alarms set",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap \"Add Alarm\" to create your first alarm",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AlarmItemCard(alarm: com.lightalarm.nativeapp.data.AlarmConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${alarm.repeatDays.displayName} • ${alarm.theme.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { /* TODO: Implement toggle */ },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkAccent,
                    checkedTrackColor = DarkAccent.copy(alpha = 0.5f)
                )
            )
        }
    }
}
