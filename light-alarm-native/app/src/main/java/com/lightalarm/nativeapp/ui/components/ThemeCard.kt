package com.lightalarm.nativeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lightalarm.nativeapp.data.LightTheme
import com.lightalarm.nativeapp.ui.theme.*

@Composable
fun ThemeCard(
    theme: LightTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentOrange.copy(alpha = 0.1f) else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = when (theme) {
                            LightTheme.SUNRISE -> listOf(SunriseStart, SunriseEnd)
                            LightTheme.GREEN_GRASS -> listOf(GreenGrassStart, GreenGrassEnd)
                            LightTheme.BLUE_SEA -> listOf(BlueSeaStart, BlueSeaEnd)
                            LightTheme.SEPHORA_BLUE -> listOf(SephoraBlueStart, SephoraBlueEnd)
                            LightTheme.PASTEL_GREEN -> listOf(PastelGreenStart, PastelGreenEnd)
                            LightTheme.AURORA -> listOf(AuroraStart, AuroraEnd)
                            LightTheme.PINK_OCEAN -> listOf(PinkOceanStart, PinkOceanEnd)
                            LightTheme.LAVENDER -> listOf(LavenderStart, LavenderEnd)
                            LightTheme.ALL_NIGHT -> listOf(AllNightStart, AllNightEnd)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = AccentOrange,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = WhiteText
                )
            }
        }
    }
}
