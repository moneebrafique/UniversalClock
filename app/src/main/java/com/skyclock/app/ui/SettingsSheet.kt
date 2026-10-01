package com.skyclock.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    theme: AppTheme,
    grid: Boolean,
    showAnalog: Boolean,
    onTheme: (AppTheme) -> Unit,
    onGrid: (Boolean) -> Unit,
    onShowAnalog: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SheetColor) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp)) {
            Text(
                "Settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            SectionTitle("Background")
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                APP_THEMES.forEach { t ->
                    val selected = t.id == theme.id
                    Column(
                        Modifier.clip(RoundedCornerShape(16.dp)).clickable { onTheme(t) }.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(width = 72.dp, height = 104.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .themeBackground(t)
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) Accent else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Box(
                                    Modifier.size(26.dp).clip(CircleShape).background(Accent),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.Rounded.Check, null, tint = Color(0xFF2B1700), modifier = Modifier.size(18.dp)) }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            t.name, fontSize = 12.sp,
                            color = if (selected) Accent else Color.White.copy(alpha = 0.8f),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }

            SectionTitle("Layout")
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LayoutOption(Icons.Rounded.ViewAgenda, "List", "1 per row, scroll", !grid, Modifier.weight(1f)) { onGrid(false) }
                LayoutOption(Icons.Rounded.GridView, "Grid", "2 per row, fits screen", grid, Modifier.weight(1f)) { onGrid(true) }
            }

            Row(
                Modifier.fillMaxWidth().clickable { onShowAnalog(!showAnalog) }.padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Show analog clock", color = Color.White, fontSize = 16.sp)
                    Text(
                        "On clock cards in List layout",
                        color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp,
                    )
                }
                Switch(
                    checked = showAnalog,
                    onCheckedChange = onShowAnalog,
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = Color(0xFF2B1700)),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 10.dp),
    )
}

@Composable
private fun LayoutOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier.clip(RoundedCornerShape(16.dp))
            .background(if (selected) Accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (selected) Accent else Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) Accent else Color.White)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}
