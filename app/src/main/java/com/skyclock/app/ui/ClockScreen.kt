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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyclock.app.data.City
import com.skyclock.app.data.CityStore
import com.skyclock.app.data.MAX_CLOCKS
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val store = remember { CityStore(context) }
    var cities by remember { mutableStateOf(store.load()) }
    var grid by remember { mutableStateOf(store.gridLayout) }
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

    // Drag & drop reordering (same order is used by both layouts)
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val onMove: (Int, Int) -> Unit = { from, to ->
        if (from != to && from in cities.indices && to in cities.indices) {
            update(cities.toMutableList().apply { add(to, removeAt(from)) })
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    val reorderList = rememberReorderableLazyListState(listState) { from, to -> onMove(from.index, to.index) }
    val reorderGrid = rememberReorderableLazyGridState(gridState) { from, to -> onMove(from.index, to.index) }

    var showAdd by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<City?>(null) }
    val local = zoned(epochMs, ZoneId.systemDefault())

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AppBgTop, AppBgMid, AppBgBottom)))) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(start = 24.dp, end = 12.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("World Clock", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${local.format(FMT_DATE)} · ${local.format(FMT_TIME_FULL)}",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp,
                    )
                }
                IconButton(onClick = {
                    grid = !grid
                    store.gridLayout = grid
                }) {
                    Icon(
                        if (grid) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                        contentDescription = if (grid) "List layout" else "Grid layout",
                        tint = Color.White,
                    )
                }
                FilledTonalButton(onClick = { showAdd = true }, enabled = cities.size < MAX_CLOCKS) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add city", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${cities.size}/$MAX_CLOCKS")
                }
            }
            if (cities.size > 1) {
                Text(
                    "Hold a clock and drag to reorder",
                    color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp,
                    modifier = Modifier.padding(start = 24.dp, bottom = 6.dp),
                )
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    cities.isEmpty() -> EmptyState { showAdd = true }

                    grid -> BoxWithConstraints(Modifier.fillMaxSize()) {
                        // 4 rows always fit the screen exactly – no scrolling needed for 8 clocks.
                        val vPad = 6.dp
                        val gap = 12.dp
                        val cellH = ((maxHeight - vPad * 2 - gap * 3) / 4).coerceAtLeast(118.dp)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = vPad),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(gap),
                        ) {
                            gridItems(cities, key = { it.id }) { city ->
                                ReorderableItem(reorderGrid, key = city.id) { dragging ->
                                    ClockCard(
                                        city = city,
                                        epochMs = epochMs,
                                        twinkle = twinkle,
                                        compact = true,
                                        isDragging = dragging,
                                        onClick = { detail = city },
                                        modifier = Modifier.height(cellH).longPressDraggableHandle(
                                            onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(cities, key = { it.id }) { city ->
                            ReorderableItem(reorderList, key = city.id) { dragging ->
                                ClockCard(
                                    city = city,
                                    epochMs = epochMs,
                                    twinkle = twinkle,
                                    compact = false,
                                    isDragging = dragging,
                                    onClick = { detail = city },
                                    modifier = Modifier.fillMaxWidth().height(176.dp).longPressDraggableHandle(
                                        onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                    ),
                                )
                            }
                        }
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

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
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
        Button(onClick = onAdd) { Text("Add a city") }
    }
}

/** Slide to see every clock at a different moment, e.g. "what time is it in London when it's 9 PM here?" */
@Composable
private fun TimeTravelBar(minutes: Float, onChange: (Float) -> Unit) {
    Surface(
        color = SheetColor.copy(alpha = 0.97f),
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
