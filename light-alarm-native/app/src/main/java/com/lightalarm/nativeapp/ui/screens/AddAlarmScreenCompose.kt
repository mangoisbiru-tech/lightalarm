package com.lightalarm.nativeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lightalarm.nativeapp.data.*
import com.lightalarm.nativeapp.ui.components.ThemeCard
import com.lightalarm.nativeapp.ui.theme.*
import com.lightalarm.nativeapp.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreenCompose(
    onNavigateBack: () -> Unit,
    viewModel: AlarmViewModel = viewModel()
) {
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedRepeat by remember { mutableStateOf(RepeatDays.DAILY) }
    var selectedSound by remember { mutableStateOf("Classic Alarm") }
    var selectedTheme by remember { mutableStateOf(LightTheme.SUNRISE) }
    
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Alarm icon",
                        tint = AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alarms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Time Selector Section
            TimeSelectorSection(
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                onHourChange = { selectedHour = it },
                onMinuteChange = { selectedMinute = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Repeat Options Section
            RepeatOptionsSection(
                selectedRepeat = selectedRepeat,
                onRepeatChange = { selectedRepeat = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sound Selector Section
            SoundSelectorSection(
                selectedSound = selectedSound,
                onSoundChange = { selectedSound = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Theme Selector Section
            ThemeSelectorSection(
                selectedTheme = selectedTheme,
                onThemeChange = { selectedTheme = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save Button
            Button(
                onClick = {
                    val newAlarm = AlarmConfig(
                        hour = selectedHour,
                        minute = selectedMinute,
                        repeatDays = selectedRepeat,
                        sound = selectedSound,
                        theme = selectedTheme
                    )
                    viewModel.addAlarm(newAlarm)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimeSelectorSection(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Set Alarm Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time picker using sliders for simplicity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hour",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%02d", selectedHour),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Slider(
                        value = selectedHour.toFloat(),
                        onValueChange = { onHourChange(it.toInt()) },
                        valueRange = 0f..23f,
                        modifier = Modifier.width(120.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Minute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%02d", selectedMinute),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Slider(
                        value = selectedMinute.toFloat(),
                        onValueChange = { onMinuteChange(it.toInt()) },
                        valueRange = 0f..59f,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatOptionsSection(
    selectedRepeat: RepeatDays,
    onRepeatChange: (RepeatDays) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Repeat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RepeatDays.values().forEach { repeat ->
                    FilterChip(
                        onClick = { onRepeatChange(repeat) },
                        label = { Text(repeat.displayName) },
                        selected = selectedRepeat == repeat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundSelectorSection(
    selectedSound: String,
    onSoundChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Sound",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SoundLibrary.sounds) { sound ->
                    FilterChip(
                        onClick = { onSoundChange(sound.name) },
                        label = { Text(sound.name) },
                        selected = selectedSound == sound.name
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSelectorSection(
    selectedTheme: LightTheme,
    onThemeChange: (LightTheme) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Light Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(LightTheme.values().toList()) { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = selectedTheme == theme,
                        onClick = { onThemeChange(theme) }
                    )
                }
            }
        }
    }
}
