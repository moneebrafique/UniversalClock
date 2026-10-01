package com.skyclock.app.sky

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

/** Times are epoch milliseconds; null when the sun doesn't rise/set that day (polar regions). */
data class SunTimes(val sunrise: Long?, val sunset: Long?, val noon: Long)

/**
 * Offline solar calculations (NOAA-style approximations, accurate to about a minute).
 * Everything is computed from the device clock and each city's coordinates — no internet.
 */
object Sun {
    private const val RAD = PI / 180.0
    /** Sun's centre at -0.833° = standard sunrise/sunset (accounts for refraction + disc size). */
    private const val HORIZON = -0.833

    /** Sun elevation in degrees above the horizon at the given moment and place. */
    fun elevation(epochMs: Long, lat: Double, lon: Double): Double {
        val n = epochMs / 86_400_000.0 + 2440587.5 - 2451545.0 // days since J2000
        val meanLon = norm360(280.460 + 0.9856474 * n)
        val g = norm360(357.528 + 0.9856003 * n) * RAD
        val lambda = (meanLon + 1.915 * sin(g) + 0.020 * sin(2 * g)) * RAD
        val eps = (23.439 - 0.0000004 * n) * RAD
        val ra = atan2(cos(eps) * sin(lambda), cos(lambda))
        val dec = asin(sin(eps) * sin(lambda))
        val gmst = norm360(280.46061837 + 360.98564736629 * n)
        val hourAngle = (gmst + lon) * RAD - ra
        val latR = lat * RAD
        return asin(sin(latR) * sin(dec) + cos(latR) * cos(dec) * cos(hourAngle)) / RAD
    }

    fun times(date: LocalDate, zone: ZoneId, lat: Double, lon: Double): SunTimes {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val step = 10 * 60_000L
        var prevT = start
        var prevD = elevation(start, lat, lon) - HORIZON
        var rise: Long? = null
        var set: Long? = null
        var bestT = start
        var bestE = -999.0
        var t = start + step
        while (t <= end) {
            val e = elevation(t, lat, lon)
            val d = e - HORIZON
            if (e > bestE) { bestE = e; bestT = t }
            if (prevD < 0 && d >= 0 && rise == null) rise = refine(prevT, t, lat, lon)
            if (prevD >= 0 && d < 0 && set == null) set = refine(prevT, t, lat, lon)
            prevT = t
            prevD = d
            t += step
        }
        return SunTimes(rise, set, bestT)
    }

    private fun refine(a0: Long, b0: Long, lat: Double, lon: Double): Long {
        var a = a0
        var b = b0
        val sa = sign(elevation(a, lat, lon) - HORIZON)
        repeat(20) {
            val m = (a + b) / 2
            if (sign(elevation(m, lat, lon) - HORIZON) == sa) a = m else b = m
        }
        return (a + b) / 2
    }

    /** 0 = new moon, 0.25 = first quarter, 0.5 = full, 0.75 = last quarter. */
    fun moonPhase(epochMs: Long): Double {
        val jd = epochMs / 86_400_000.0 + 2440587.5
        val p = ((jd - 2451550.1) / 29.530588853) % 1.0
        return if (p < 0) p + 1 else p
    }

    private fun norm360(x: Double): Double {
        val r = x % 360.0
        return if (r < 0) r + 360.0 else r
    }
}
