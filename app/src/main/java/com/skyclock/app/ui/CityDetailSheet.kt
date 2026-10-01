package com.skyclock.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyclock.app.data.City
import com.skyclock.app.sky.SkyState
import com.skyclock.app.sky.Sun
import com.skyclock.app.sky.SunTimes
import com.skyclock.app.sky.skyStateFor
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailSheet(
    city: City,
    travelMs: Long,
    twinkle: State<Float>,
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
) {
    // Frame-accurate clock so the second hand sweeps smoothly.
    val nowMs by produceState(System.currentTimeMillis()) {
        while (true) {
            withFrameMillis { }
            value = System.currentTimeMillis()
        }
    }
    val epochMs = nowMs + travelMs
    val zone = remember(city) { ZoneId.of(city.zone) }
    val z = zoned(epochMs, zone)
    val local = zoned(epochMs, ZoneId.systemDefault())
    val date = z.toLocalDate()
    val times = remember(city, date) { Sun.times(date, zone, city.lat, city.lon) }
    val tomorrow = remember(city, date) { Sun.times(date.plusDays(1), zone, city.lat, city.lon) }
    val state = remember(city, epochMs / 30_000L, times) { skyStateFor(city, epochMs, times) }
    val stars = remember(city.id) { makeStars(city.id.hashCode() * 31, 70) }
    val dst = remember(city, date) { zone.rules.isDaylightSavings(z.toInstant()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetColor,
        dragHandle = null,
    ) {
        Column(Modifier.navigationBarsPadding()) {
            // Big sky with the clock
            Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
                Canvas(Modifier.matchParentSize()) { drawSkyScene(state, stars, twinkle.value, scale = 1.5f) }
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.25f))),
                    ),
                )
                Column(Modifier.align(Alignment.TopStart).padding(22.dp)) {
                    Text(city.name, style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, shadow = TextShadow), color = Color.White)
                    Text(city.country, style = TextStyle(fontSize = 14.sp, shadow = TextShadow), color = Color.White.copy(alpha = 0.85f))
                }
                Box(Modifier.align(Alignment.TopEnd).padding(18.dp)) { PhaseChip(state.phase) }
                Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val ms = (epochMs % 1000L) / 1000f
                    AnalogClock(z.hour, z.minute, z.second + ms, Modifier.size(170.dp))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(z.format(FMT_TIME), style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Light, shadow = TextShadow), color = Color.White)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            z.format(FMT_AMPM),
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, shadow = TextShadow),
                            color = Color.White, modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }

            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(z.format(FMT_DATE_YEAR), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append(z.format(FMT_ZONE)).append(" · ").append(utcLabel(z))
                        if (dst) append(" · Daylight saving")
                        append(" · ")
                        val off = offsetLabel(z, local)
                        append(if (off == "Same time") "Same time as you" else "$off from you")
                    },
                    color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp,
                )

                Spacer(Modifier.height(14.dp))
                SunArc(state, Modifier.fillMaxWidth().height(120.dp))
                Text(
                    sunStatus(epochMs, times, tomorrow),
                    color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile(Icons.Rounded.WbTwilight, "Sunrise", times.sunrise?.let { zoned(it, zone).format(FMT_TIME_FULL) } ?: "—", Modifier.weight(1f))
                    InfoTile(Icons.Rounded.NightsStay, "Sunset", times.sunset?.let { zoned(it, zone).format(FMT_TIME_FULL) } ?: "—", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val sr = times.sunrise
                    val ss = times.sunset
                    InfoTile(
                        Icons.Rounded.Timelapse, "Day length",
                        if (sr != null && ss != null && ss > sr) durationLabel(ss - sr) else if (state.elevation > 0) "24h" else "0h",
                        Modifier.weight(1f),
                    )
                    InfoTile(Icons.Rounded.LightMode, "Solar noon", zoned(times.noon, zone).format(FMT_TIME_FULL), Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Remove this clock")
                }
            }
        }
    }
}

private fun sunStatus(now: Long, today: SunTimes, tomorrow: SunTimes): String {
    val sr = today.sunrise
    val ss = today.sunset
    return when {
        sr != null && now < sr -> "Sunrise in ${durationLabel(sr - now)}"
        ss != null && now < ss -> "Sunset in ${durationLabel(ss - now)}"
        tomorrow.sunrise != null -> "Sunrise in ${durationLabel(tomorrow.sunrise - now)}"
        else -> "The sun doesn't rise today"
    }
}

@Composable
private fun InfoTile(icon: ImageVector, label: String, value: String, modifier: Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.06f)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Sun's path across the sky today, with its current position. */
@Composable
private fun SunArc(state: SkyState, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val base = h * 0.88f
        val left = w * 0.06f
        val right = w * 0.94f
        val rx = (right - left) / 2f
        val cx = (left + right) / 2f
        val ry = h * 0.78f
        val rect = Rect(cx - rx, base - ry, cx + rx, base + ry)

        drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, base), Offset(w, base), strokeWidth = 1.dp.toPx())
        drawPath(
            Path().apply { arcTo(rect, 180f, 180f, true) },
            Color.White.copy(alpha = 0.30f),
            style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))),
        )
        drawCircle(Color.White.copy(alpha = 0.6f), 3.dp.toPx(), Offset(left, base))
        drawCircle(Color.White.copy(alpha = 0.6f), 3.dp.toPx(), Offset(right, base))

        val f = state.dayFraction
        if (f != null) {
            drawPath(
                Path().apply { arcTo(rect, 180f, 180f * f, true) },
                Brush.horizontalGradient(listOf(Color(0xFFFF7A2F), Color(0xFFFFD54F))),
                style = Stroke(3.dp.toPx(), cap = StrokeCap.Round),
            )
            val ang = PI * (1.0 - f)
            val c = Offset((cx + rx * cos(ang)).toFloat(), (base - ry * sin(ang)).toFloat())
            drawCircle(
                Brush.radialGradient(listOf(Color(0x88FFC24D), Color.Transparent), center = c, radius = 26.dp.toPx()),
                radius = 26.dp.toPx(), center = c,
            )
            drawCircle(state.sky.sunColor, 9.dp.toPx(), c)
        } else {
            // Night: a small moon resting below the horizon line
            val c = Offset(cx, base - h * 0.30f)
            drawCircle(
                Brush.radialGradient(listOf(Color(0x449FB4FF), Color.Transparent), center = c, radius = 24.dp.toPx()),
                radius = 24.dp.toPx(), center = c,
            )
            drawCircle(Color(0xFFE8ECFF), 8.dp.toPx(), c)
        }
    }
}
