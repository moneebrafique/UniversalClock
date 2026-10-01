package com.skyclock.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyclock.app.data.City
import com.skyclock.app.sky.DayPhase
import com.skyclock.app.sky.Sun
import com.skyclock.app.sky.skyStateFor
import java.time.ZoneId

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockCard(
    city: City,
    epochMs: Long,
    twinkle: State<Float>,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = remember(city) { ZoneId.of(city.zone) }
    val z = zoned(epochMs, zone)
    val local = zoned(epochMs, ZoneId.systemDefault())
    val date = z.toLocalDate()
    val sunTimes = remember(city, date) { Sun.times(date, zone, city.lat, city.lon) }
    val state = remember(city, epochMs / 60_000L, sunTimes) { skyStateFor(city, epochMs, sunTimes) }
    val stars = remember(city.id) { makeStars(city.id.hashCode()) }
    var menu by remember { mutableStateOf(false) }

    Box(
        modifier.fillMaxWidth().height(176.dp)
            .clip(RoundedCornerShape(30.dp))
            .combinedClickable(onClick = onClick, onLongClick = { menu = true }),
    ) {
        Canvas(Modifier.matchParentSize()) { drawSkyScene(state, stars, twinkle.value) }
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.22f), Color.Transparent, Color.Black.copy(alpha = 0.10f))),
            ),
        )
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
                        style = TextStyle(
                            fontSize = 46.sp, fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp, shadow = TextShadow,
                        ),
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
                    PhaseChip(state.phase)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${dayLabel(z, local)} · ${offsetLabel(z, local)}",
                        style = TextStyle(fontSize = 12.sp, shadow = TextShadow),
                        color = Color.White.copy(alpha = 0.90f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Box(Modifier.align(Alignment.TopEnd)) {
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Move up") },
                    leadingIcon = { Icon(Icons.Rounded.ArrowUpward, null) },
                    enabled = canMoveUp,
                    onClick = { menu = false; onMoveUp() },
                )
                DropdownMenuItem(
                    text = { Text("Move down") },
                    leadingIcon = { Icon(Icons.Rounded.ArrowDownward, null) },
                    enabled = canMoveDown,
                    onClick = { menu = false; onMoveDown() },
                )
                DropdownMenuItem(
                    text = { Text("Remove") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                    onClick = { menu = false; onRemove() },
                )
            }
        }
    }
}
