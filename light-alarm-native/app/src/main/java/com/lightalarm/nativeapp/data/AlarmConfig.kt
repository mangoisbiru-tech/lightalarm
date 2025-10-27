package com.lightalarm.nativeapp.data

import java.util.Calendar

data class AlarmConfig(
    val id: Int = 0,
    val hour: Int = 7,
    val minute: Int = 0,
    val repeatDays: RepeatDays = RepeatDays.DAILY,
    val sound: String = "Classic Alarm",
    val theme: LightTheme = LightTheme.SUNRISE,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RepeatDays(val displayName: String) {
    DAILY("Daily"),
    WEEKENDS("Weekends"),
    WEEKDAYS("Weekdays"),
    CUSTOM("Custom")
}

enum class LightTheme(
    val displayName: String,
    val startColor: String,
    val endColor: String
) {
    SUNRISE("Sunrise", "#FF6600", "#FFCC00"),
    GREEN_GRASS("Green Grass", "#00FF00", "#00CC00"),
    BLUE_SEA("Blue Sea", "#00D2FF", "#3A7BD5"),
    SEPHORA_BLUE("Sephora Blue", "#00CED1", "#008080"),
    PASTEL_GREEN("Pastel Green", "#00FF00", "#00CC00"),
    AURORA("Aurora", "#00FF66", "#0099FF"),
    PINK_OCEAN("Pink Ocean", "#FF1493", "#FF69B4"),
    LAVENDER("Lavender", "#9370DB", "#7B68EE"),
    ALL_NIGHT("All Night", "#FFD700", "#FFA500")
}

data class SoundOption(
    val name: String,
    val category: String,
    val resourceId: Int? = null
)

object SoundLibrary {
    val sounds = listOf(
        SoundOption("Classic Alarm", "Classic"),
        SoundOption("Gentle Chime", "Classic"),
        SoundOption("Birds Chirping", "Nature"),
        SoundOption("Ocean Waves", "Nature"),
        SoundOption("Rain Sounds", "Ambient"),
        SoundOption("Forest Ambience", "Ambient")
    )
}
