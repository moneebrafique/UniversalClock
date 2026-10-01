package com.skyclock.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFFFFB74D)
val SheetColor = Color(0xFF11152C)

@Composable
fun SkyClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Color(0xFF2B1700),
            secondary = Color(0xFF9FA8FF),
            surface = SheetColor,
            surfaceContainerLow = SheetColor,
            background = Color(0xFF070A18),
        ),
        content = content,
    )
}
