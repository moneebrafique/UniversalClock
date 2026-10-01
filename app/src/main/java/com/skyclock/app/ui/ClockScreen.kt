package com.skyclock.app.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val store = remember { CityStore(context) }
    var cities by remember { mutableStateOf(store.load()) }
    var grid by remember { mutableStateOf(store.gridLayout) }
    var showAnalog by remember { mutableStateOf(store.showAnalog) }
    var theme by remember { mutableStateOf(themeById(store.themeId)) }
    fun update(list: List<City>) {
        cities = list
        store.save(list)
    }

    // Status bar icons dark on the light theme, light otherwise.
    val activity = context as? Activity
    SideEffect {
        activity?.window?.let { w ->
            WindowCompat.getInsetsController(w, w.decorView).apply {
                isAppearanceLightStatusBars = theme.light
                isAppearanceLightNavigationBars = theme.light
            }
        }
    }

    // Ticks exactly on each new second.
    val nowMs by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000L - System.currentTimeMillis() % 1000L)
            value = System.currentTimeMillis()
        }
    }
    var travelMin by remember { mutableFloatStateOf(0f) }
    var travelOpen by remember { mutableStateOf(false) }
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
    var showSettings by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<City?>(null) }
    val local = zoned(epochMs, ZoneId.systemDefault())
    val fg = theme.content

    Box(Modifier.fillMaxSize().themeBackground(theme)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("World Clock", color = fg, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${local.format(FMT_DATE)} · ${local.format(FMT_TIME_FULL)}",
                        color = fg.copy(alpha = 0.6f), fontSize = 13.sp,
                    )
                }
                IconButton(onClick = { travelOpen = !travelOpen }) {
                    Icon(
                        Icons.Rounded.History, contentDescription = "Time travel",
                        tint = if (travelOpen || travelMin != 0f) Accent else fg,
                    )
                }
                IconButton(onClick = {
                    grid = !grid
                    store.gridLayout = grid
                }) {
                    Icon(
                        if (grid) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                        contentDescription = if (grid) "List layout" else "Grid layout",
                        tint = fg,
                    )
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Rounded.Tune, contentDescription = "Settings", tint = fg)
                }
                FilledTonalIconButton(onClick = { showAdd = true }, enabled = cities.size < MAX_CLOCKS) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add city")
                }
            }

            // Time travel panel (opened from the header – nothing at the bottom to touch by accident)
            AnimatedVisibility(
                visible = travelOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                TimeTravelPanel(
                    minutes = travelMin,
                    onChange = { travelMin = it },
                    onClose = { travelOpen = false },
                )
            }
            // Always-visible warning while showing a shifted time
            AnimatedVisibility(visible = travelMin != 0f && !travelOpen) {
                TravelBanner(travelMin) {
                    travelMin = 0f
                }
            }

            Text(
                "${cities.size} of $MAX_CLOCKS clocks" + if (cities.size > 1) " · hold and drag to reorder" else "",
                color = fg.copy(alpha = 0.4f), fontSize = 11.sp,
                modifier = Modifier.padding(start = 22.dp, top = 2.dp, bottom = 6.dp),
            )

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    cities.isEmpty() -> EmptyState(fg) { showAdd = true }

                    grid -> BoxWithConstraints(Modifier.fillMaxSize()) {
                        // 4 rows always fit the screen exactly – no scrolling needed for 8 clocks.
                        val vPad = 6.dp
                        val gap = 12.dp
                        val cellH = ((maxHeight - vPad * 2 - gap * 3) / 4).coerceAtLeast(110.dp)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = vPad, bottom = vPad),
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
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp),
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
                                    showAnalog = showAnalog,
                                    onClick = { detail = city },
                                    modifier = Modifier.fillMaxWidth().height(170.dp).longPressDraggableHandle(
                                        onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                    ),
                                )
                            }
                        }
                    }
                }
            }
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
    if (showSettings) {
        SettingsSheet(
            theme = theme,
            grid = grid,
            showAnalog = showAnalog,
            onTheme = { theme = it; store.themeId = it.id },
            onGrid = { grid = it; store.gridLayout = it },
            onShowAnalog = { showAnalog = it; store.showAnalog = it },
            onDismiss = { showSettings = false },
        )
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

private fun travelLabel(minutes: Float): String {
    val m = minutes.roundToInt()
    if (m == 0) return "Now"
    val sign = if (m > 0) "+" else "−"
    val h = abs(m) / 60
    val mm = abs(m) % 60
    return "$sign${h}h" + if (mm > 0) " ${mm}m" else ""
}

@Composable
private fun TimeTravelPanel(minutes: Float, onChange: (Float) -> Unit, onClose: () -> Unit) {
    Surface(
        color = SheetColor.copy(alpha = 0.97f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Column(Modifier.padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Time travel  ${travelLabel(minutes)}",
                    color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f),
                )
                if (minutes != 0f) TextButton(onClick = { onChange(0f) }) { Text("Now") }
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close", tint = Color.White) }
            }
            Text(
                "See every clock at a different moment – e.g. plan a call.",
                color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp,
            )
            Slider(
                value = minutes,
                onValueChange = { onChange((it / 15f).roundToInt() * 15f) },
                valueRange = -720f..720f,
                modifier = Modifier.padding(end = 10.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Accent,
                    activeTrackColor = Accent.copy(alpha = 0.7f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                ),
            )
            Row(Modifier.padding(end = 10.dp)) {
                Text("−12h", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text("Now", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text("+12h", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun TravelBanner(minutes: Float, onReset: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(Accent)
            .clickable(onClick = onReset)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.History, null, tint = Color(0xFF2B1700), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Showing time ${travelLabel(minutes)} from now",
            color = Color(0xFF2B1700), fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text("Back to now", color = Color(0xFF2B1700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyState(fg: Color, onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.Public, null, tint = Accent, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        Text("No clocks yet", color = fg, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Add up to $MAX_CLOCKS cities to see their time, day and night at a glance.",
            color = fg.copy(alpha = 0.6f), textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAdd) { Text("Add a city") }
    }
}
