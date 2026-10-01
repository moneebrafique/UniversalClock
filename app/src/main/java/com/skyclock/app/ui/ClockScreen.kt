package com.skyclock.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyclock.app.data.City
import com.skyclock.app.data.CityStore
import com.skyclock.app.data.MAX_CLOCKS
import kotlinx.coroutines.delay
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val store = remember { CityStore(context) }
    var cities by remember { mutableStateOf(store.load()) }
    fun update(list: List<City>) {
        cities = list
        store.save(list)
    }

    // Ticks exactly on each new second.
    val nowMs by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000L - System.currentTimeMillis() % 1000L)
            value = System.currentTimeMillis()
        }
    }
    var travelMin by remember { mutableFloatStateOf(0f) }
    val travelMs = travelMin.roundToInt() * 60_000L
    val epochMs = nowMs + travelMs

    val twinkle = rememberInfiniteTransition(label = "stars").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "twinkle",
    )

    var showAdd by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<City?>(null) }
    val local = zoned(epochMs, ZoneId.systemDefault())

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF060914), Color(0xFF0D1230), Color(0xFF171A3C))),
        ),
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(start = 24.dp, end = 16.dp, top = 18.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("World Clock", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${local.format(FMT_DATE)} · ${local.format(FMT_TIME_FULL)}",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp,
                    )
                }
                FilledTonalButton(onClick = { showAdd = true }, enabled = cities.size < MAX_CLOCKS) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add city", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${cities.size}/$MAX_CLOCKS")
                }
            }

            if (cities.isEmpty()) {
                Column(
                    Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Rounded.Public, null, tint = Accent, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No clocks yet", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Add up to $MAX_CLOCKS cities to see their time, day and night at a glance.",
                        color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showAdd = true }) { Text("Add a city") }
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    itemsIndexed(cities, key = { _, c -> c.id }) { i, city ->
                        ClockCard(
                            city = city,
                            epochMs = epochMs,
                            twinkle = twinkle,
                            canMoveUp = i > 0,
                            canMoveDown = i < cities.lastIndex,
                            onClick = { detail = city },
                            onMoveUp = { update(cities.toMutableList().apply { add(i - 1, removeAt(i)) }) },
                            onMoveDown = { update(cities.toMutableList().apply { add(i + 1, removeAt(i)) }) },
                            onRemove = { update(cities - city) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            TimeTravelBar(travelMin, onChange = { travelMin = it })
        }
    }

    if (showAdd) {
        AddCitySheet(
            existing = cities,
            epochMs = epochMs,
            onDismiss = { showAdd = false },
        ) { city ->
            if (cities.size < MAX_CLOCKS && city !in cities) update(cities + city)
            showAdd = false
        }
    }
    detail?.let { city ->
        CityDetailSheet(
            city = city,
            travelMs = travelMs,
            twinkle = twinkle,
            onDismiss = { detail = null },
            onRemove = {
                update(cities - city)
                detail = null
            },
        )
    }
}

/** Slide to see every clock at a different moment, e.g. "what time is it in London when it's 9 PM here?" */
@Composable
private fun TimeTravelBar(minutes: Float, onChange: (Float) -> Unit) {
    Surface(
        color = Color(0xFF12162E).copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 6.dp,
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                val m = minutes.roundToInt()
                val label = if (m == 0) "Time travel" else {
                    val sign = if (m > 0) "+" else "−"
                    val h = abs(m) / 60
                    val mm = abs(m) % 60
                    "Time travel  $sign${h}h" + if (mm > 0) " ${mm}m" else ""
                }
                Text(label, color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(visible = m != 0) {
                    TextButton(onClick = { onChange(0f) }) { Text("Back to now") }
                }
            }
            Slider(
                value = minutes,
                onValueChange = { onChange((it / 15f).roundToInt() * 15f) },
                valueRange = -720f..720f,
                colors = SliderDefaults.colors(
                    thumbColor = Accent,
                    activeTrackColor = Accent.copy(alpha = 0.7f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                ),
            )
            Row {
                Text("−12h", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text("Now", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text("+12h", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
    }
}
