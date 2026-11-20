package com.pokecharge.animation.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.pokecharge.animation.engine.AnimationStageEvent
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Renders one type's procedurally-generated elemental effect.
 *
 * Every type gets its own (particle silhouette × motion curve × colour ×
 * density) combination from [TypeEffectLibrary] - see that file for why
 * no two types are simple recolors of each other. Particle positions are
 * computed analytically from a single looping `time` value rather than a
 * mutable per-frame particle list, so there is nothing to leak: when this
 * composable leaves composition the animation simply stops.
 *
 * [intensity] (0..1, already battery-stage-smoothed by `AnimationEngine`)
 * scales both particle count and opacity so the effect visibly "wakes up"
 * as the battery/charging progress increases, without ever snapping.
 */
@Composable
fun TypeEffectCanvas(
    spec: TypeEffectSpec,
    intensity: Float,
    event: AnimationStageEvent,
    brightness: Float,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "pc-effect-loop")
    val time by infinite.loopingTime(spec.periodMillis)

    // One-shot full-charge burst: a short expanding shockwave layered on
    // top of the regular ambient effect. Triggered purely by the discrete
    // event, independent of the continuous `time` loop above.
    val burst = remember { Animatable(0f) }
    LaunchedEffect(event) {
        if (event == AnimationStageEvent.FULL_CHARGE) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(900, easing = LinearEasing))
        }
    }
    val disconnectFade = remember { Animatable(1f) }
    LaunchedEffect(event) {
        if (event == AnimationStageEvent.DISCONNECT) {
            disconnectFade.snapTo(1f)
            disconnectFade.animateTo(0.35f, tween(500))
        } else if (event == AnimationStageEvent.CHARGE_START || event == AnimationStageEvent.CHARGING) {
            disconnectFade.animateTo(1f, tween(400))
        }
    }

    val activeCount = (1 + (spec.baseParticleCount * intensity)).toInt().coerceAtLeast(1)
    val globalAlpha = (0.35f + 0.65f * intensity) * brightness * disconnectFade.value

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val minDim = min(w, h)

        // Soft radial vignette behind the character, tinted per-type.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(spec.backgroundTint.copy(alpha = 0.55f * globalAlpha), Color.Transparent),
                center = Offset(cx, cy),
                radius = minDim * 0.62f,
            ),
        )

        for (i in 0 until activeCount) {
            val seed = (i * 0.618034f) % 1f
            val localT = (time + seed) % 1f
            val rnd = Random(i * 7919 + 13)
            drawParticle(spec, localT, seed, rnd, cx, cy, minDim, globalAlpha)
        }

        if (burst.value > 0f) {
            drawFullChargeBurst(spec, burst.value, cx, cy, minDim)
        }
    }
}

@Composable
private fun androidx.compose.animation.core.InfiniteTransition.loopingTime(periodMillis: Int) =
    this.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pc-effect-time",
    )

private fun DrawScope.drawParticle(
    spec: TypeEffectSpec,
    t: Float,
    seed: Float,
    rnd: Random,
    cx: Float,
    cy: Float,
    minDim: Float,
    globalAlpha: Float,
) {
    val color = if (seed > 0.5f) spec.primaryColor else spec.secondaryColor
    val r = minDim * 0.46f

    when (spec.motion) {
        MotionPattern.JAGGED_STRIKE -> {
            // Short, staccato electric arcs that flash in and out.
            val visible = ((t * 5f) % 1f) < 0.35f
            if (!visible) return
            val ang = (seed * 2 * PI).toFloat()
            val anchor = Offset(cx + cos(ang) * r * 0.9f, cy + sin(ang) * r * 0.6f - r * 0.2f)
            val alpha = (0.5f + 0.5f * sin(t * 2 * PI * 6).toFloat()) * globalAlpha
            drawBolt(anchor, minDim * 0.16f, color, alpha, rnd)
        }
        MotionPattern.RISE_FLICKER -> {
            val y = cy + r * 0.7f - t * r * 1.5f
            val x = cx + sin(t * 2 * PI * 2 + seed * 10).toFloat() * minDim * 0.08f
            val alpha = (1f - t) * globalAlpha * (0.6f + 0.4f * sin(t * 2 * PI * 6).toFloat())
            drawFlame(Offset(x, y), minDim * (0.05f + 0.03f * (1 - t)), color, alpha)
        }
        MotionPattern.FALL_AND_RIPPLE -> {
            val x = cx + (seed - 0.5f) * minDim * 1.1f
            if (t < 0.7f) {
                val localT = t / 0.7f
                val y = cy - r * 0.9f + localT * r * 1.5f
                drawDroplet(Offset(x, y), minDim * 0.03f, color, globalAlpha)
            } else {
                val rippleT = (t - 0.7f) / 0.3f
                val y = cy + r * 0.6f
                drawCircle(color, radius = minDim * 0.18f * rippleT, center = Offset(x, y), alpha = (1 - rippleT) * globalAlpha * 0.6f, style = Stroke(width = 3f))
            }
        }
        MotionPattern.FLUTTER_DOWN -> {
            val y = cy - r + t * r * 2.2f
            val sway = sin(t * 2 * PI * 3 + seed * 6).toFloat() * minDim * 0.1f
            val x = cx + (seed - 0.5f) * minDim * 0.8f + sway
            val alpha = (1f - abs(t - 0.5f) * 1.2f).coerceIn(0f, 1f) * globalAlpha
            rotate(t * 360f, Offset(x, y)) { drawLeaf(Offset(x, y), minDim * 0.045f, color, alpha) }
        }
        MotionPattern.DRIFT_SETTLE -> {
            val ang = seed * 2 * PI + t * 2 * PI * 0.3f
            val radius = r * (0.35f + 0.5f * ((seed * 13) % 1f))
            val x = cx + cos(ang).toFloat() * radius
            val y = cy + sin(ang).toFloat() * radius * 0.6f
            val alpha = (0.5f + 0.5f * sin(t * 2 * PI * 2).toFloat()) * globalAlpha
            if (spec.shape == ParticleShape.SNOWFLAKE) drawSnowflake(Offset(x, y), minDim * 0.03f, color, alpha)
            else drawOrb(Offset(x, y), minDim * 0.025f, color, alpha)
        }
        MotionPattern.IMPACT_RADIATE -> {
            val visible = ((t * 4f) % 1f) < 0.4f
            if (!visible) return
            val ang = seed * 2 * PI
            val dist = ((t * 4f) % 1f) / 0.4f * r * 0.8f
            val start = Offset(cx + cos(ang).toFloat() * dist * 0.4f, cy + sin(ang).toFloat() * dist * 0.4f)
            val end = Offset(cx + cos(ang).toFloat() * dist, cy + sin(ang).toFloat() * dist)
            drawLine(color, start, end, strokeWidth = minDim * 0.012f, cap = StrokeCap.Round, alpha = globalAlpha)
        }
        MotionPattern.BUBBLE_UP -> {
            val y = cy + r * 0.8f - t * r * 1.8f
            val x = cx + sin(t * 2 * PI * 2 + seed * 8).toFloat() * minDim * 0.1f
            val size = minDim * (0.02f + 0.03f * t)
            drawCircle(color, size, Offset(x, y), alpha = (1 - t) * globalAlpha * 0.8f, style = Stroke(width = 2.5f))
        }
        MotionPattern.SWIRL_LOW -> {
            val ang = seed * 2 * PI + t * 2 * PI
            val radius = r * (0.5f + 0.3f * ((seed * 9) % 1f))
            val x = cx + cos(ang).toFloat() * radius
            val y = cy + r * 0.75f + sin(ang).toFloat() * radius * 0.12f
            drawDustMote(Offset(x, y), minDim * 0.018f, color, globalAlpha * 0.8f)
        }
        MotionPattern.GLIDE_SIDEWAYS -> {
            val x = ((t + seed) % 1f) * (minDim * 1.4f) - minDim * 0.2f
            val y = cy - r * 0.6f + sin(seed * 2 * PI).toFloat() * r * 0.5f + sin(t * 2 * PI * 2).toFloat() * minDim * 0.04f
            drawFeather(Offset(x, y), minDim * 0.045f, color, globalAlpha * 0.85f, t)
        }
        MotionPattern.ORBIT_CENTER -> {
            val ang = seed * 2 * PI + t * 2 * PI
            val radius = r * (0.55f + 0.25f * sin(t * 2 * PI + seed * 5).toFloat())
            val x = cx + cos(ang).toFloat() * radius
            val y = cy + sin(ang).toFloat() * radius
            drawOrb(Offset(x, y), minDim * 0.028f, color, globalAlpha)
        }
        MotionPattern.ZIGZAG_DART -> {
            val segment = (t * 6).toInt() % 4
            val dx = when (segment) { 0 -> -1f; 1 -> 1f; 2 -> -0.6f; else -> 0.8f }
            val dy = when (segment) { 0 -> -1f; 1 -> 0.4f; 2 -> 1f; else -> -0.5f }
            val x = cx + dx * r * 0.5f * ((seed * 2) % 1f + 0.4f)
            val y = cy + dy * r * 0.5f * ((seed * 3) % 1f + 0.4f)
            drawWingGlint(Offset(x, y), minDim * 0.03f, color, globalAlpha)
        }
        MotionPattern.TUMBLE_FALL -> {
            val y = cy - r + (t * t) * r * 2.1f
            val x = cx + (seed - 0.5f) * minDim * 0.9f
            rotate(t * 540f, Offset(x, y)) { drawShard(Offset(x, y), minDim * 0.035f, color, globalAlpha) }
        }
        MotionPattern.FLICKER_GLITCH -> {
            val visible = ((t * 3f + seed) % 1f) < 0.5f
            if (!visible) return
            val glitchX = if (rnd.nextFloat() < 0.15f) (rnd.nextFloat() - 0.5f) * minDim * 0.06f else 0f
            val ang = seed * 2 * PI
            val x = cx + cos(ang).toFloat() * r * 0.5f + glitchX
            val y = cy + sin(ang).toFloat() * r * 0.35f
            drawSmokeWisp(Offset(x, y), minDim * 0.06f, color, globalAlpha * 0.6f)
        }
        MotionPattern.SWEEP_TRAIL -> {
            val ang = (t * 2 * PI + seed * 2 * PI).toFloat()
            val x = cx + cos(ang) * r * 0.85f
            val y = cy - r * 0.2f + sin(ang) * r * 0.3f
            drawAuraRibbon(Offset(x, y), minDim * 0.05f, color, globalAlpha, ang)
        }
        MotionPattern.PULSE_RING -> {
            val ringT = (t + seed * 0.3f) % 1f
            drawCircle(spec.primaryColor, radius = r * ringT, center = Offset(cx, cy), alpha = (1 - ringT) * globalAlpha * 0.5f, style = Stroke(width = 4f))
        }
        MotionPattern.SWEEP_SHINE -> {
            val x = ((t + seed) % 1f) * minDim * 1.3f - minDim * 0.15f
            drawMetalGlint(Offset(x, cy), minDim * 0.4f, color, globalAlpha * 0.5f)
        }
        MotionPattern.WEAVE -> {
            val x = cx + sin(t * 2 * PI + seed * 6).toFloat() * r * 0.7f
            val y = cy + cos(t * 2 * PI * 1.3f + seed * 4).toFloat() * r * 0.4f
            drawRibbonMote(Offset(x, y), minDim * 0.025f, color, globalAlpha)
        }
        MotionPattern.TWINKLE -> {
            val alpha = (0.4f + 0.6f * sin(t * 2 * PI * 3 + seed * 10).toFloat()).coerceIn(0f, 1f) * globalAlpha
            val ang = seed * 2 * PI
            val x = cx + cos(ang).toFloat() * r * 0.7f
            val y = cy + sin(ang).toFloat() * r * 0.5f
            drawStar(Offset(x, y), minDim * 0.02f, color, alpha)
        }
    }
}

// --- Shape primitives -------------------------------------------------

private fun DrawScope.drawBolt(anchor: Offset, length: Float, color: Color, alpha: Float, rnd: Random) {
    var x = anchor.x
    var y = anchor.y - length / 2
    val path = androidx.compose.ui.graphics.Path().apply { moveTo(x, y) }
    repeat(4) {
        x += (rnd.nextFloat() - 0.5f) * length * 0.5f
        y += length / 4
        path.lineTo(x, y)
    }
    drawPath(path, color, alpha = alpha, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawFlame(center: Offset, size: Float, color: Color, alpha: Float) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - size * 1.4f)
        quadraticBezierTo(center.x + size, center.y - size * 0.3f, center.x, center.y + size)
        quadraticBezierTo(center.x - size, center.y - size * 0.3f, center.x, center.y - size * 1.4f)
        close()
    }
    drawPath(path, color, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawDroplet(center: Offset, size: Float, color: Color, alpha: Float) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - size * 1.3f)
        quadraticBezierTo(center.x + size, center.y + size * 0.4f, center.x, center.y + size)
        quadraticBezierTo(center.x - size, center.y + size * 0.4f, center.x, center.y - size * 1.3f)
        close()
    }
    drawPath(path, color, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawLeaf(center: Offset, size: Float, color: Color, alpha: Float) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - size)
        quadraticBezierTo(center.x + size, center.y, center.x, center.y + size)
        quadraticBezierTo(center.x - size, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawSnowflake(center: Offset, size: Float, color: Color, alpha: Float) {
    for (i in 0 until 6) {
        val ang = (i * 60).toDouble() * PI / 180
        val end = Offset(center.x + (cos(ang) * size).toFloat(), center.y + (sin(ang) * size).toFloat())
        drawLine(color, center, end, strokeWidth = 1.6f, alpha = alpha.coerceIn(0f, 1f))
    }
}

private fun DrawScope.drawOrb(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(color, radius, center, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawDustMote(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(color, radius, center, alpha = (alpha * 0.7f).coerceIn(0f, 1f))
}

private fun DrawScope.drawFeather(center: Offset, size: Float, color: Color, alpha: Float, t: Float) {
    rotate(sin(t * 2 * PI).toFloat() * 20f, center) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y - size)
            quadraticBezierTo(center.x + size * 0.6f, center.y, center.x, center.y + size)
            quadraticBezierTo(center.x - size * 0.2f, center.y, center.x, center.y - size)
            close()
        }
        drawPath(path, color, alpha = alpha.coerceIn(0f, 1f))
    }
}

private fun DrawScope.drawWingGlint(center: Offset, size: Float, color: Color, alpha: Float) {
    drawLine(color, Offset(center.x - size, center.y), Offset(center.x + size, center.y), strokeWidth = 2.2f, cap = StrokeCap.Round, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawShard(center: Offset, size: Float, color: Color, alpha: Float) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.8f, center.y + size * 0.6f)
        lineTo(center.x - size * 0.8f, center.y + size * 0.6f)
        close()
    }
    drawPath(path, color, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawSmokeWisp(center: Offset, size: Float, color: Color, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = alpha.coerceIn(0f, 1f)), Color.Transparent), center = center, radius = size),
        radius = size,
        center = center,
    )
}

private fun DrawScope.drawAuraRibbon(center: Offset, size: Float, color: Color, alpha: Float, angle: Float) {
    rotate(angle * 180f / PI.toFloat(), center) {
        drawRoundRect(
            color,
            topLeft = Offset(center.x - size, center.y - size * 0.25f),
            size = androidx.compose.ui.geometry.Size(size * 2, size * 0.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.25f),
            alpha = alpha.coerceIn(0f, 1f),
        )
    }
}

private fun DrawScope.drawMetalGlint(center: Offset, height: Float, color: Color, alpha: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = alpha.coerceIn(0f, 1f)), Color.Transparent),
            start = Offset(center.x - height * 0.15f, 0f),
            end = Offset(center.x + height * 0.15f, 0f),
        ),
        topLeft = Offset(center.x - height * 0.15f, center.y - height),
        size = androidx.compose.ui.geometry.Size(height * 0.3f, height * 2),
    )
}

private fun DrawScope.drawRibbonMote(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(color, radius, center, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color, alpha: Float) {
    drawLine(color, Offset(center.x - size, center.y), Offset(center.x + size, center.y), strokeWidth = 1.4f, alpha = alpha.coerceIn(0f, 1f))
    drawLine(color, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 1.4f, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawFullChargeBurst(spec: TypeEffectSpec, progress: Float, cx: Float, cy: Float, minDim: Float) {
    val radius = minDim * 0.1f + progress * minDim * 0.55f
    val alpha = (1f - progress).coerceIn(0f, 1f)
    drawCircle(spec.primaryColor, radius, Offset(cx, cy), alpha = alpha * 0.55f, style = Stroke(width = 6f * (1f - progress * 0.5f)))
    drawCircle(spec.secondaryColor, radius * 0.6f, Offset(cx, cy), alpha = alpha * 0.35f, style = Stroke(width = 3f))
}
