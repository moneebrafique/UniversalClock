package com.skyclock.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFFFFB74D)
val SheetColor = Color(0xFF16161D)
val AppBgTop = Color(0xFF09090D)
val AppBgMid = Color(0xFF101015)
val AppBgBottom = Color(0xFF17171E)

@Composable
fun SkyClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Color(0xFF2B1700),
            secondary = Color(0xFF9FA8FF),
            surface = SheetColor,
            surfaceContainerLow = SheetColor,
            background = Color(0xFF09090D),
        ),
        content = content,
    )
}
