package com.example.spartanlauncher

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Windows 10 style boot screen: black, white "SpartanOS" wordmark, five orbiting dots. */
object BootScreen {
    private var shown = false

    fun showOnColdStart(activity: Activity) {
        if (shown) return
        shown = true
        val root = activity.window.decorView as ViewGroup
        val view = BootScreenView(activity)
        root.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT))
        val start = SystemClock.uptimeMillis()
        fun dismiss() {
            view.animate().alpha(0f).setDuration(450L)
                .setInterpolator(android.view.animation.PathInterpolator(0.1f, 0.9f, 0.2f, 1f))
                .withEndAction { root.removeView(view) }.start()
        }
        // Wait for Start's first frame, but keep the boot screen up at least ~3.8s.
        view.post {
            val left = (3800L - (SystemClock.uptimeMillis() - start)).coerceAtLeast(0L)
            view.postDelayed(::dismiss, left)
        }
    }
}

private class BootScreenView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.selawik_light)
        textSize = 46f * density
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val startTime = SystemClock.uptimeMillis()

    init {
        setBackgroundColor(Color.BLACK)
        isClickable = true // swallow touches while booting
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        // Wordmark where the Windows logo sits (~20-37% height).
        val y = h * 0.29f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText("SpartanOS", w / 2f, y, textPaint)

        // W10 loader: 5 dots chase around a ring, fast through the bottom, slow over the top.
        val cx = w / 2f; val cy = h * 0.75f
        val radius = 20f * density
        val dot = 2f * density
        val cycle = 3600f; val travel = 2900f
        val now = (SystemClock.uptimeMillis() - startTime).toFloat()
        for (i in 0 until 5) {
            val t = (now - i * 150f).mod(cycle)
            if (now - i * 150f < 0 || t > travel) continue
            val p = t / travel // 0..1 over two laps
            // slow-fast-slow per revolution: fast through the bottom, slow over the top
            val eased = p + sin(2 * PI * 2 * p).toFloat() / (2 * PI.toFloat() * 2) * 0.85f
            val angle = PI / 2 + eased * 4 * PI // start at bottom
            val alpha = when { p < 0.06f -> p / 0.06f; p > 0.94f -> (1 - p) / 0.06f; else -> 1f }
            dotPaint.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
            canvas.drawCircle(cx + (radius * cos(angle)).toFloat(),
                cy + (radius * sin(angle)).toFloat(), dot, dotPaint)
        }
        if (isAttachedToWindow) postInvalidateOnAnimation()
    }
}