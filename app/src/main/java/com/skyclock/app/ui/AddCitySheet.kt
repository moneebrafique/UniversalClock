package com.skyclock.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyclock.app.data.Cities
import com.skyclock.app.data.City
import com.skyclock.app.sky.Sun
import com.skyclock.app.sky.skyStateFor
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCitySheet(
    existing: List<City>,
    epochMs: Long,
    onDismiss: () -> Unit,
    onAdd: (City) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        val q = query.trim()
        Cities.all.filter { q.isEmpty() || it.name.contains(q, true) || it.country.contains(q, true) }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetColor,
    ) {
        Column(Modifier.fillMaxHeight(0.92f)) {
            Text(
                "Add a city",
                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search city or country") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(results, key = { it.id }) { city ->
                    val added = city in existing
                    val zone = remember(city) { ZoneId.of(city.zone) }
                    val z = zoned(epochMs, zone)
                    val phase = remember(city, epochMs / 600_000L) {
                        skyStateFor(city, epochMs, Sun.times(z.toLocalDate(), zone, city.lat, city.lon)).phase
                    }
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable(enabled = !added) { onAdd(city) }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(phaseIcon(phase), null, tint = Accent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(city.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                            Text(city.country, color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                        }
                        if (added) {
                            Icon(Icons.Rounded.CheckCircle, "Added", tint = Accent)
                        } else {
                            Text(z.format(FMT_TIME_FULL), color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
