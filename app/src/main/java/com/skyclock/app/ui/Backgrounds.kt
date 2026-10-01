package com.skyclock.app.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** App background themes. Glows are soft coloured light spots over the base gradient. */
data class AppTheme(
    val id: String,
    val name: String,
    val top: Color,
    val bottom: Color,
    val glow1: Color,
    val glow2: Color,
    val light: Boolean = false,
) {
    /** Text / icon colour on top of this background. */
    val content: Color get() = if (light) Color(0xFF1B1D24) else Color.White
}

val APP_THEMES = listOf(
    AppTheme("aurora", "Aurora", Color(0xFF06080B), Color(0xFF0D1013), Color(0x3810B5A8), Color(0x2E8A4DF0)),
    AppTheme("graphite", "Graphite", Color(0xFF09090D), Color(0xFF18181F), Color(0x14FFFFFF), Color(0x00000000)),
    AppTheme("ember", "Ember", Color(0xFF0B0707), Color(0xFF170D0A), Color(0x30FF7A2F), Color(0x26C0306A)),
    AppTheme("forest", "Forest", Color(0xFF050A07), Color(0xFF0D1711), Color(0x2C2FAE62), Color(0x1E1E7F8F)),
    AppTheme("rose", "Rose", Color(0xFF0C0609), Color(0xFF180B12), Color(0x2CE0407A), Color(0x228A3FD0)),
    AppTheme("amoled", "Pure Black", Color(0xFF000000), Color(0xFF000000), Color(0x00000000), Color(0x00000000)),
    AppTheme("daylight", "Daylight", Color(0xFFF6F3EE), Color(0xFFE4E8EF), Color(0x66FFC680), Color(0x558EC5FC), light = true),
)

const val DEFAULT_THEME_ID = "daylight"

fun themeById(id: String?): AppTheme =
    APP_THEMES.firstOrNull { it.id == (id ?: DEFAULT_THEME_ID) } ?: APP_THEMES.first { it.id == DEFAULT_THEME_ID }

fun Modifier.themeBackground(t: AppTheme): Modifier = drawBehind {
    drawRect(Brush.verticalGradient(listOf(t.top, t.bottom)))
    val r = size.maxDimension * 0.75f
    if (t.glow1.alpha > 0f) {
        val c = Offset(size.width * 0.10f, size.height * 0.04f)
        drawCircle(Brush.radialGradient(listOf(t.glow1, Color.Transparent), center = c, radius = r), radius = r, center = c)
    }
    if (t.glow2.alpha > 0f) {
        val c = Offset(size.width * 0.95f, size.height * 0.92f)
        drawCircle(Brush.radialGradient(listOf(t.glow2, Color.Transparent), center = c, radius = r), radius = r, center = c)
    }
}
