package com.skyclock.app.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyclock.app.data.City
import com.skyclock.app.sky.DayPhase
import com.skyclock.app.sky.SkyState
import com.skyclock.app.sky.Sun
import com.skyclock.app.sky.skyStateFor
import java.time.ZoneId
import java.time.ZonedDateTime

val TextShadow = Shadow(Color.Black.copy(alpha = 0.40f), Offset(0f, 2f), 10f)

fun phaseIcon(p: DayPhase): ImageVector = when (p) {
    DayPhase.NIGHT -> Icons.Rounded.NightsStay
    DayPhase.DAWN, DayPhase.SUNRISE, DayPhase.GOLDEN, DayPhase.SUNSET -> Icons.Rounded.WbTwilight
    DayPhase.MORNING, DayPhase.AFTERNOON -> Icons.Rounded.WbSunny
    DayPhase.MIDDAY -> Icons.Rounded.LightMode
    DayPhase.DUSK -> Icons.Rounded.DarkMode
}

@Composable
fun PhaseChip(phase: DayPhase) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(phaseIcon(phase), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(phase.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

class ClockData(
    val zoned: ZonedDateTime,
    val local: ZonedDateTime,
    val state: SkyState,
    val stars: List<Star>,
)

@Composable
fun rememberClockData(city: City, epochMs: Long): ClockData {
    val zone = remember(city) { ZoneId.of(city.zone) }
    val z = zoned(epochMs, zone)
    val local = zoned(epochMs, ZoneId.systemDefault())
    val date = z.toLocalDate()
    val sunTimes = remember(city, date) { Sun.times(date, zone, city.lat, city.lon) }
    val state = remember(city, epochMs / 60_000L, sunTimes) { skyStateFor(city, epochMs, sunTimes) }
    val stars = remember(city.id) { makeStars(city.id.hashCode()) }
    return ClockData(z, local, state, stars)
}

/**
 * One clock. [compact] = grid tile (2 per row); otherwise a wide card.
 * A soft glow in the sky's own colour plus a light glass edge keep every card — even a dark
 * night sky — clearly separated from the app background.
 */
@Composable
fun ClockCard(
    city: City,
    epochMs: Long,
    twinkle: State<Float>,
    compact: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val d = rememberClockData(city, epochMs)
    val shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
    val scale by animateFloatAsState(if (isDragging) 1.04f else 1f, label = "scale")
    val elevation by animateDpAsState(if (isDragging) 28.dp else 12.dp, label = "elev")
    val glow = d.state.sky.bottom

    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation, shape, ambientColor = glow, spotColor = glow)
            .clip(shape)
            .clickable(onClick = onClick),
    ) {
        Canvas(Modifier.matchParentSize()) { drawSkyScene(d.state, d.stars, twinkle.value, scale = if (compact) 0.8f else 1f) }
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.22f), Color.Transparent, Color.Black.copy(alpha = 0.10f))),
            ),
        )
        if (compact) CompactContent(city, d) else WideContent(city, d)

        // Glass edge (accent while dragging)
        Box(
            Modifier.matchParentSize().border(
                width = if (isDragging) 2.dp else 1.dp,
                brush = if (isDragging) Brush.verticalGradient(listOf(Accent, Accent.copy(alpha = 0.5f)))
                else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.06f))),
                shape = shape,
            ),
        )
    }
}

@Composable
private fun WideContent(city: City, d: ClockData) {
    val z = d.zoned
    Row(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnalogClock(z.hour, z.minute, z.second.toFloat(), Modifier.size(100.dp))
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                city.name,
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, shadow = TextShadow),
                color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                city.country,
                style = TextStyle(fontSize = 13.sp, shadow = TextShadow),
                color = Color.White.copy(alpha = 0.80f), maxLines = 1,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    z.format(FMT_TIME),
                    style = TextStyle(fontSize = 46.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp, shadow = TextShadow),
                    color = Color.White,
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    z.format(FMT_AMPM),
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium, shadow = TextShadow),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 9.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PhaseChip(d.state.phase)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${dayLabel(z, d.local)} · ${offsetLabel(z, d.local)}",
                    style = TextStyle(fontSize = 12.sp, shadow = TextShadow),
                    color = Color.White.copy(alpha = 0.90f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CompactContent(city: City, d: ClockData) {
    val z = d.zoned
    Column(Modifier.fillMaxSize().padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    city.name,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, shadow = TextShadow),
                    color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    city.country,
                    style = TextStyle(fontSize = 11.sp, shadow = TextShadow),
                    color = Color.White.copy(alpha = 0.80f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(6.dp))
            AnalogClock(z.hour, z.minute, z.second.toFloat(), Modifier.size(42.dp))
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                z.format(FMT_TIME),
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Light, letterSpacing = (-0.5).sp, shadow = TextShadow),
                color = Color.White, maxLines = 1,
            )
            Spacer(Modifier.width(3.dp))
            Text(
                z.format(FMT_AMPM),
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, shadow = TextShadow),
                color = Color.White, modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(phaseIcon(d.state.phase), contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "${d.state.phase.label} · ${offsetLabel(z, d.local)}",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, shadow = TextShadow),
                color = Color.White.copy(alpha = 0.92f), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
