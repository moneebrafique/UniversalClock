package com.skyclock.app.sky

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.skyclock.app.data.City
import java.time.Instant
import java.time.ZoneId

enum class DayPhase(val label: String) {
    NIGHT("Night"),
    DAWN("Dawn"),
    SUNRISE("Sunrise"),
    MORNING("Morning"),
    MIDDAY("Midday"),
    AFTERNOON("Afternoon"),
    GOLDEN("Golden hour"),
    SUNSET("Sunset"),
    DUSK("Dusk"),
}

data class Sky(
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val starAlpha: Float,
    val sunColor: Color,
)

data class SkyState(
    val elevation: Double,
    val rising: Boolean,
    val phase: DayPhase,
    val sky: Sky,
    /** 0 at sunrise .. 1 at sunset; null outside daylight or on polar days/nights. */
    val dayFraction: Float?,
    val moonPhase: Double,
)

fun skyStateFor(city: City, epochMs: Long, times: SunTimes): SkyState {
    val e = Sun.elevation(epochMs, city.lat, city.lon)
    val rising = Sun.elevation(epochMs + 10 * 60_000L, city.lat, city.lon) > e
    val hour = Instant.ofEpochMilli(epochMs).atZone(ZoneId.of(city.zone)).hour
    val sr = times.sunrise
    val ss = times.sunset
    val fraction = if (sr != null && ss != null && ss > sr && epochMs in sr..ss) {
        ((epochMs - sr).toDouble() / (ss - sr)).toFloat()
    } else null
    return SkyState(e, rising, phaseFor(e, rising, hour), SkyPalette.sky(e, rising), fraction, Sun.moonPhase(epochMs))
}

private fun phaseFor(e: Double, rising: Boolean, hour: Int): DayPhase = when {
    e < -12 -> DayPhase.NIGHT
    e < -0.833 -> if (rising) DayPhase.DAWN else DayPhase.DUSK
    e < 4 -> if (rising) DayPhase.SUNRISE else DayPhase.SUNSET
    e < 10 && !rising -> DayPhase.GOLDEN
    hour < 11 -> DayPhase.MORNING
    hour < 14 -> DayPhase.MIDDAY
    else -> DayPhase.AFTERNOON
}

/** Sky colours keyed by sun elevation; mornings are cooler/pinker, evenings warmer/redder. */
object SkyPalette {
    private class Key(val e: Float, val top: Long, val mid: Long, val bottom: Long)

    private val MORNING = listOf(
        Key(-90f, 0xFF0A1238, 0xFF152056, 0xFF263478),
        Key(-18f, 0xFF0C1540, 0xFF1A2662, 0xFF2E3C86),
        Key(-10f, 0xFF0B1442, 0xFF26306E, 0xFF4A3F7A),
        Key(-4f, 0xFF1B2B6B, 0xFF6A5A9E, 0xFFF29E8E),
        Key(0f, 0xFF2E4C9A, 0xFFB07AA8, 0xFFFFB27A),
        Key(5f, 0xFF3F7CD0, 0xFF8FB4E8, 0xFFFFD6A0),
        Key(15f, 0xFF2C7BE5, 0xFF6FB1F2, 0xFFBFE0FF),
        Key(90f, 0xFF1565C0, 0xFF42A5F5, 0xFFA9D8FF),
    )

    private val EVENING = listOf(
        Key(-90f, 0xFF0A1238, 0xFF152056, 0xFF263478),
        Key(-18f, 0xFF0D1340, 0xFF1C2060, 0xFF33307E),
        Key(-10f, 0xFF0E1240, 0xFF35286A, 0xFF5C3466),
        Key(-4f, 0xFF1F2466, 0xFF7A4A8E, 0xFFFF7A6B),
        Key(0f, 0xFF35407F, 0xFFC0607A, 0xFFFF9452),
        Key(5f, 0xFF4A6FB8, 0xFFE89A7A, 0xFFFFC27A),
        Key(15f, 0xFF2C7BE5, 0xFF7BB0E8, 0xFFFFE2B8),
        Key(90f, 0xFF1565C0, 0xFF42A5F5, 0xFFA9D8FF),
    )

    fun sky(elevation: Double, rising: Boolean): Sky {
        val keys = if (rising) MORNING else EVENING
        val e = elevation.toFloat()
        var i = keys.indexOfLast { it.e <= e }.coerceAtLeast(0)
        if (i >= keys.lastIndex) i = keys.lastIndex - 1
        val a = keys[i]
        val b = keys[i + 1]
        val t = ((e - a.e) / (b.e - a.e)).coerceIn(0f, 1f)
        return Sky(
            top = lerp(Color(a.top), Color(b.top), t),
            mid = lerp(Color(a.mid), Color(b.mid), t),
            bottom = lerp(Color(a.bottom), Color(b.bottom), t),
            starAlpha = ((-e - 3f) / 9f).coerceIn(0f, 1f),
            sunColor = lerp(Color(0xFFFF7A2F), Color(0xFFFFF4D6), ((e + 1f) / 25f).coerceIn(0f, 1f)),
        )
    }
}
