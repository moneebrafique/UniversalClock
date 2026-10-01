package com.skyclock.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.skyclock.app.sky.SkyState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Star(val x: Float, val y: Float, val r: Float, val p: Float)

fun makeStars(seed: Int, count: Int = 45): List<Star> {
    val rnd = Random(seed)
    return List(count) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.72f, 0.5f + rnd.nextFloat() * 1.3f, rnd.nextFloat() * 6.28f) }
}

/** Sky gradient, twinkling stars, moon with its real phase, sun with glow, and layered hills. */
fun DrawScope.drawSkyScene(s: SkyState, stars: List<Star>, twinkle: Float, scale: Float = 1f) {
    val w = size.width
    val h = size.height
    val horizon = h * 0.80f
    val e = s.elevation.toFloat()

    drawRect(Brush.verticalGradient(listOf(s.sky.top, s.sky.mid, s.sky.bottom)))

    // Stars
    if (s.sky.starAlpha > 0f) {
        stars.forEach { st ->
            val a = (s.sky.starAlpha * (0.55f + 0.45f * sin(twinkle + st.p))).coerceIn(0f, 1f)
            drawCircle(Color.White.copy(alpha = a), radius = st.r * density, center = Offset(st.x * w, st.y * h))
        }
    }

    // Moon
    if (e < -1f) {
        val alpha = ((-e - 1f) / 6f).coerceIn(0f, 1f)
        val c = Offset(w * 0.80f, h * 0.24f)
        val r = 9.dp.toPx() * scale
        drawCircle(
            Brush.radialGradient(listOf(Color(0x55DDE6FF), Color.Transparent), center = c, radius = r * 4.5f),
            radius = r * 4.5f, center = c, alpha = alpha,
        )
        drawCircle(Color(0xFFE8ECF8).copy(alpha = 0.10f * alpha), r, c)
        drawPath(moonPath(c, r, s.moonPhase), Color(0xFFF4F6FF), alpha = alpha)
    }

    // Sun
    if (e > -4f) {
        val f = s.dayFraction ?: if (s.rising) 0f else 1f
        val x = w * (0.14f + 0.72f * f)
        val y = horizon - ((e.coerceIn(-4f, 70f) + 4f) / 74f) * (horizon - h * 0.12f)
        val r = 11.dp.toPx() * scale
        val c = Offset(x, y)
        drawCircle(
            Brush.radialGradient(listOf(s.sky.sunColor.copy(alpha = 0.55f), Color.Transparent), center = c, radius = r * 5.5f),
            radius = r * 5.5f, center = c,
        )
        drawCircle(s.sky.sunColor, r, c)
    }

    // Hills
    val back = lerp(s.sky.bottom, Color.Black, 0.55f)
    val front = lerp(s.sky.bottom, Color(0xFF02030A), 0.80f)
    drawPath(
        Path().apply {
            moveTo(0f, horizon)
            cubicTo(w * 0.2f, horizon - h * 0.10f, w * 0.35f, horizon + h * 0.02f, w * 0.55f, horizon - h * 0.05f)
            cubicTo(w * 0.75f, horizon - h * 0.12f, w * 0.9f, horizon - h * 0.02f, w, horizon - h * 0.06f)
            lineTo(w, h); lineTo(0f, h); close()
        },
        back,
    )
    drawPath(
        Path().apply {
            moveTo(0f, horizon + h * 0.08f)
            cubicTo(w * 0.25f, horizon + h * 0.01f, w * 0.45f, horizon + h * 0.12f, w * 0.7f, horizon + h * 0.05f)
            cubicTo(w * 0.85f, horizon + h * 0.01f, w * 0.95f, horizon + h * 0.06f, w, horizon + h * 0.04f)
            lineTo(w, h); lineTo(0f, h); close()
        },
        front,
    )
}

/** Lit part of the moon: a disc minus an offset "shadow" disc. */
private fun moonPath(c: Offset, r: Float, phase: Double): Path {
    val p = phase.toFloat()
    val d = if (p <= 0.5f) 2f * r * (p / 0.5f) else 2f * r * ((1f - p) / 0.5f)
    val dir = if (p <= 0.5f) -1f else 1f // waxing: lit on the right
    val disc = Path().apply { addOval(Rect(c, r)) }
    val shadow = Path().apply { addOval(Rect(Offset(c.x + dir * d, c.y), r)) }
    return Path().apply { op(disc, shadow, PathOperation.Difference) }
}

/** Glassy analog clock face. */
@Composable
fun AnalogClock(
    hour: Int,
    minute: Int,
    second: Float,
    modifier: Modifier,
    accent: Color = Color(0xFFFFB74D),
) {
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = center
        drawCircle(Color.White.copy(alpha = 0.14f), r, c)
        drawCircle(Color.Black.copy(alpha = 0.10f), r * 0.92f, c)
        drawCircle(Color.White.copy(alpha = 0.50f), r - 0.75.dp.toPx(), c, style = Stroke(1.5.dp.toPx()))

        val big = r > 60.dp.toPx()
        for (i in 0 until 60) {
            val major = i % 5 == 0
            if (!major && !big) continue
            val a = i * 6.0 * PI / 180.0
            val outer = r - 5.dp.toPx()
            val len = when {
                i % 15 == 0 -> 8.dp
                major -> 5.dp
                else -> 2.dp
            }.toPx()
            drawLine(
                Color.White.copy(alpha = if (major) 0.9f else 0.35f),
                polar(c, outer - len, a), polar(c, outer, a),
                strokeWidth = (if (i % 15 == 0) 2.5.dp else if (major) 1.5.dp else 1.dp).toPx(),
                cap = StrokeCap.Round,
            )
        }

        val minF = minute + second / 60f
        val hourF = (hour % 12) + minF / 60f
        val hourA = hourF * 30.0 * PI / 180.0
        val minA = minF * 6.0 * PI / 180.0
        val secA = second * 6.0 * PI / 180.0

        drawLine(Color.White, c, polar(c, r * 0.50, hourA), strokeWidth = r * 0.085f, cap = StrokeCap.Round)
        drawLine(Color.White, c, polar(c, r * 0.72, minA), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
        drawLine(accent, polar(c, -r * 0.16, secA), polar(c, r * 0.80, secA), strokeWidth = r * 0.025f + 0.5f, cap = StrokeCap.Round)
        drawCircle(accent, r * 0.075f, c)
        drawCircle(Color.White, r * 0.03f, c)
    }
}

private fun polar(c: Offset, len: Float, angle: Double) =
    Offset(c.x + (len * sin(angle)).toFloat(), c.y - (len * cos(angle)).toFloat())

private fun polar(c: Offset, len: Double, angle: Double) = polar(c, len.toFloat(), angle)
