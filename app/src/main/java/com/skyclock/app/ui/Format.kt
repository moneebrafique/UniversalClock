package com.skyclock.app.ui

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

val FMT_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.US)
val FMT_AMPM: DateTimeFormatter = DateTimeFormatter.ofPattern("a", Locale.US)
val FMT_TIME_FULL: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
val FMT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.US)
val FMT_DATE_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US)
val FMT_ZONE: DateTimeFormatter = DateTimeFormatter.ofPattern("zzz", Locale.US)

fun zoned(epochMs: Long, zone: ZoneId): ZonedDateTime = Instant.ofEpochMilli(epochMs).atZone(zone)

/** "+9h", "−4h 30m" or "Same time" compared with the phone's own time zone. */
fun offsetLabel(city: ZonedDateTime, local: ZonedDateTime): String {
    val diff = city.offset.totalSeconds - local.offset.totalSeconds
    if (diff == 0) return "Same time"
    val sign = if (diff > 0) "+" else "−"
    val m = abs(diff) / 60
    val h = m / 60
    val mm = m % 60
    return sign + if (mm == 0) "${h}h" else "${h}h ${mm}m"
}

fun dayLabel(city: ZonedDateTime, local: ZonedDateTime): String =
    when (ChronoUnit.DAYS.between(local.toLocalDate(), city.toLocalDate())) {
        0L -> "Today"
        1L -> "Tomorrow"
        -1L -> "Yesterday"
        else -> city.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.US))
    }

fun durationLabel(ms: Long): String {
    val totalMin = (ms / 60_000L).coerceAtLeast(0)
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h == 0L) "${m}m" else "${h}h ${m}m"
}

fun utcLabel(z: ZonedDateTime): String {
    val id = z.offset.id
    return if (id == "Z") "UTC+0" else "UTC$id"
}
