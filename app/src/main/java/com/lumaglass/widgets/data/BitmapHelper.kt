package com.lumaglass.widgets.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object BitmapHelper {
    private const val PHOTO_DIR_NAME = "widget_photos"
    private const val MAX_WIDGET_IMAGE_DIM = 512

    // 5x3 Dot-Matrix Font Map for digits '0'..'9' and ':'
    private val DOT_MATRIX_GLYPHS: Map<Char, IntArray> = mapOf(
        '0' to intArrayOf(0b111, 0b101, 0b101, 0b101, 0b111),
        '1' to intArrayOf(0b010, 0b110, 0b010, 0b010, 0b111),
        '2' to intArrayOf(0b111, 0b001, 0b111, 0b100, 0b111),
        '3' to intArrayOf(0b111, 0b001, 0b111, 0b001, 0b111),
        '4' to intArrayOf(0b101, 0b101, 0b111, 0b001, 0b001),
        '5' to intArrayOf(0b111, 0b100, 0b111, 0b001, 0b111),
        '6' to intArrayOf(0b111, 0b100, 0b111, 0b101, 0b111),
        '7' to intArrayOf(0b111, 0b001, 0b001, 0b010, 0b010),
        '8' to intArrayOf(0b111, 0b101, 0b111, 0b101, 0b111),
        '9' to intArrayOf(0b111, 0b101, 0b111, 0b001, 0b111),
        ':' to intArrayOf(0b000, 0b010, 0b000, 0b010, 0b000)
    )

    /**
     * Copies and downscales a user-picked photo into internal storage so Glance widgets
     * never fail due to URI permission expiration or cause OutOfMemoryError on launchers.
     */
    fun importAndDownscalePhoto(context: Context, sourceUri: Uri): String? {
        return runCatching {
            val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val sampleSize = calculateInSampleSize(
                boundsOpts.outWidth,
                boundsOpts.outHeight,
                MAX_WIDGET_IMAGE_DIM,
                MAX_WIDGET_IMAGE_DIM
            )
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val scaled = scaleDownIfNeeded(decoded, MAX_WIDGET_IMAGE_DIM)
            val outFile = File(dir, "photo_${UUID.randomUUID()}.jpg")
            FileOutputStream(outFile).use { fos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 88, fos)
            }
            if (scaled !== decoded) decoded.recycle()
            scaled.recycle()
            outFile.absolutePath
        }.getOrNull()
    }

    /**
     * Loads a safe downscaled bitmap from an internal file path or content URI BEFORE provideContent.
     */
    fun loadSafeWidgetBitmap(context: Context, uriOrPath: String?, maxDim: Int = 420): Bitmap? {
        if (uriOrPath.isNullOrBlank()) return null
        return runCatching {
            val file = File(uriOrPath)
            if (file.exists()) {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim, maxDim)
                }
                BitmapFactory.decodeFile(file.absolutePath, opts)?.let { bmp ->
                    val scaled = scaleDownIfNeeded(bmp, maxDim)
                    if (scaled !== bmp) bmp.recycle()
                    scaled
                }
            } else {
                val uri = Uri.parse(uriOrPath)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim, maxDim)
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }?.let { bmp ->
                    val scaled = scaleDownIfNeeded(bmp, maxDim)
                    if (scaled !== bmp) bmp.recycle()
                    scaled
                }
            }
        }.getOrNull()
    }

    /**
     * Renders an authentic frosted liquid-glass surface bitmap matching opacity, corner radius,
     * tint preset (or Material You / Pro custom hex), surface texture, and specular border.
     */
    fun renderGlassSurfaceBitmap(
        context: Context,
        config: WidgetConfig,
        isSystemDark: Boolean,
        widthPx: Int = 360,
        heightPx: Int = 360
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = resolveGlassBaseColor(context, config, isSystemDark)
        val alphaInt = (config.opacity.coerceIn(0f, 1f) * 235f).roundToInt()

        val (topMult, bottomMult) = when (config.surfaceTexture) {
            GlassSurfaceTexture.CRYSTAL_FROST -> 1.08f to 0.80f
            GlassSurfaceTexture.MATTE_SATIN -> 0.96f to 0.92f
            GlassSurfaceTexture.PRISM_EDGE -> 1.12f to 0.72f
            GlassSurfaceTexture.SOFT_HALO -> 1.04f to 0.78f
        }
        val topAlpha = (alphaInt * topMult).roundToInt().coerceIn(0, 250)
        val bottomAlpha = (alphaInt * bottomMult).roundToInt().coerceIn(0, 242)

        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)

        val startColor = Color.argb(topAlpha, min(255, r + 16), min(255, g + 16), min(255, b + 16))
        val endColor = Color.argb(bottomAlpha, r, g, b)

        val densityFactor = widthPx / 160f
        val radiusPx = (config.cornerRadiusDp.coerceIn(4, 40) * densityFactor)
        val pad = 3f
        val rect = RectF(pad, pad, widthPx - pad, heightPx - pad)

        // Subtle outer drop shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(pad, pad + 2f, widthPx - pad, heightPx - pad + 2f),
            radiusPx,
            radiusPx,
            shadowPaint
        )

        // Frosted liquid gradient body
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                startColor, endColor, Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, radiusPx, radiusPx, fillPaint)

        // Optional inner radial halo for SOFT_HALO texture
        if (config.surfaceTexture == GlassSurfaceTexture.SOFT_HALO) {
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    widthPx * 0.5f,
                    heightPx * 0.32f,
                    widthPx * 0.55f,
                    intArrayOf(
                        Color.argb(50, 255, 255, 255),
                        Color.argb(0, 255, 255, 255)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(rect, radiusPx, radiusPx, haloPaint)
        }

        val darkText = isDarkTextSurface(context, config, isSystemDark)

        // Specular border based on surfaceTexture
        val borderAlpha = if (config.opacity < 0.1f) 115 else 155
        if (config.surfaceTexture == GlassSurfaceTexture.PRISM_EDGE) {
            val prismPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                    intArrayOf(
                        Color.argb(borderAlpha, 125, 211, 252),
                        Color.argb(borderAlpha, 216, 180, 254),
                        Color.argb(borderAlpha, 253, 230, 138)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            canvas.drawRoundRect(rect, radiusPx, radiusPx, prismPaint)
        } else {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (darkText) {
                    Color.argb(borderAlpha, 255, 255, 255)
                } else {
                    Color.argb((borderAlpha * 0.6f).roundToInt(), 255, 255, 255)
                }
                strokeWidth = if (config.surfaceTexture == GlassSurfaceTexture.MATTE_SATIN) 1.6f else 2.5f
                style = Paint.Style.STROKE
            }
            canvas.drawRoundRect(rect, radiusPx, radiusPx, borderPaint)
        }

        return bitmap
    }

    /**
     * Pre-renders the Analog Ring Clock dial onto a clean bitmap before provideContent.
     */
    fun renderAnalogClockDialBitmap(
        hour: Int,
        minute: Int,
        darkText: Boolean,
        accentColor: Int = Color.parseColor("#38BDF8"),
        sizePx: Int = 240
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx * 0.41f

        val ringTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(42, 15, 23, 42) else Color.argb(54, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.045f
        }
        canvas.drawCircle(cx, cy, radius, ringTrackPaint)

        // Hour markers
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(115, 15, 23, 42) else Color.argb(140, 248, 250, 252)
            strokeCap = Paint.Cap.ROUND
            strokeWidth = sizePx * 0.018f
        }
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val rIn = radius * 0.82f
            val rOut = radius * 0.92f
            canvas.drawLine(
                (cx + rIn * cos(angle)).toFloat(),
                (cy + rIn * sin(angle)).toFloat(),
                (cx + rOut * cos(angle)).toFloat(),
                (cy + rOut * sin(angle)).toFloat(),
                markerPaint
            )
        }

        // Minute progress arc
        val minuteSweep = (minute / 60f) * 360f
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = sizePx * 0.045f
        }
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(arcRect, -90f, minuteSweep, false, arcPaint)

        // Hour hand
        val hourAngle = Math.toRadians((((hour % 12) + minute / 60f) * 30f - 90f).toDouble())
        val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
            strokeCap = Paint.Cap.ROUND
            strokeWidth = sizePx * 0.045f
        }
        canvas.drawLine(
            cx,
            cy,
            (cx + radius * 0.50f * cos(hourAngle)).toFloat(),
            (cy + radius * 0.50f * sin(hourAngle)).toFloat(),
            handPaint
        )

        // Minute hand
        val minAngle = Math.toRadians((minute * 6f - 90f).toDouble())
        val minHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = sizePx * 0.030f
        }
        canvas.drawLine(
            cx,
            cy,
            (cx + radius * 0.72f * cos(minAngle)).toFloat(),
            (cy + radius * 0.72f * sin(minAngle)).toFloat(),
            minHandPaint
        )

        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.parseColor("#0F172A") else Color.WHITE
        }
        canvas.drawCircle(cx, cy, sizePx * 0.036f, centerPaint)
        return bmp
    }

    /**
     * Pre-renders the Circular Glass Battery Ring onto a crisp bitmap before provideContent.
     */
    fun renderBatteryRingBitmap(
        levelPercent: Int,
        isCharging: Boolean,
        darkText: Boolean,
        sizePx: Int = 220
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx * 0.39f
        val stroke = sizePx * 0.085f

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(36, 15, 23, 42) else Color.argb(46, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        canvas.drawCircle(cx, cy, radius, trackPaint)

        val ringColor = when {
            isCharging -> Color.parseColor("#22C55E")
            levelPercent <= 20 -> Color.parseColor("#F97316")
            else -> Color.parseColor("#38BDF8")
        }
        val sweep = (levelPercent.coerceIn(0, 100) / 100f) * 360f
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ringColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = stroke
        }
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, -90f, sweep, false, arcPaint)
        return bmp
    }

    /**
     * Pre-renders soft layered gradient orbs for Abstract Aurora Orb Clock.
     */
    fun renderAbstractAuraBitmap(
        darkText: Boolean,
        widthPx: Int = 280,
        heightPx: Int = 280
    ): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val orb1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                widthPx * 0.30f,
                heightPx * 0.34f,
                widthPx * 0.48f,
                intArrayOf(
                    Color.argb(if (darkText) 110 else 135, 56, 189, 248),
                    Color.argb(0, 56, 189, 248)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val orb2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                widthPx * 0.72f,
                heightPx * 0.68f,
                widthPx * 0.50f,
                intArrayOf(
                    Color.argb(if (darkText) 105 else 130, 167, 139, 250),
                    Color.argb(0, 167, 139, 250)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val orb3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                widthPx * 0.74f,
                heightPx * 0.28f,
                widthPx * 0.34f,
                intArrayOf(
                    Color.argb(if (darkText) 85 else 105, 251, 191, 36),
                    Color.argb(0, 251, 191, 36)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), orb1)
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), orb2)
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), orb3)
        return bmp
    }

    /**
     * Pre-renders flowing abstract sine-wave horizon layers.
     */
    fun renderAbstractWaveBitmap(
        darkText: Boolean,
        widthPx: Int = 420,
        heightPx: Int = 210
    ): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        fun drawWave(baseY: Float, amplitude: Float, phase: Float, colorInt: Int) {
            val path = Path()
            path.moveTo(0f, heightPx.toFloat())
            var x = 0f
            while (x <= widthPx) {
                val t = (x / widthPx) * Math.PI * 2.0 + phase
                val y = baseY + (sin(t) * amplitude).toFloat()
                path.lineTo(x, y)
                x += 8f
            }
            path.lineTo(widthPx.toFloat(), heightPx.toFloat())
            path.close()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorInt
                style = Paint.Style.FILL
            }
            canvas.drawPath(path, paint)
        }

        drawWave(
            baseY = heightPx * 0.48f,
            amplitude = heightPx * 0.12f,
            phase = 0.4f,
            colorInt = Color.argb(if (darkText) 48 else 62, 56, 189, 248)
        )
        drawWave(
            baseY = heightPx * 0.62f,
            amplitude = heightPx * 0.11f,
            phase = 2.1f,
            colorInt = Color.argb(if (darkText) 54 else 68, 167, 139, 250)
        )
        drawWave(
            baseY = heightPx * 0.76f,
            amplitude = heightPx * 0.08f,
            phase = 3.8f,
            colorInt = Color.argb(if (darkText) 58 else 74, 52, 211, 153)
        )
        return bmp
    }

    /**
     * Pre-renders a Bauhaus-inspired geometric dial for Abstract Geometric Time.
     */
    fun renderAbstractGeometricDialBitmap(
        hour: Int,
        minute: Int,
        darkText: Boolean,
        sizePx: Int = 240
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val r = sizePx * 0.36f

        canvas.drawCircle(
            cx - sizePx * 0.10f,
            cy - sizePx * 0.06f,
            r * 0.72f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(if (darkText) 60 else 80, 56, 189, 248)
            }
        )
        canvas.drawArc(
            RectF(cx - r * 0.45f, cy - r * 0.85f, cx + r * 0.95f, cy + r * 0.55f),
            -40f,
            190f,
            true,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(if (darkText) 56 else 72, 196, 181, 253)
            }
        )
        canvas.drawCircle(
            cx,
            cy,
            r,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (darkText) Color.argb(120, 15, 23, 42) else Color.argb(140, 248, 250, 252)
                style = Paint.Style.STROKE
                strokeWidth = sizePx * 0.022f
            }
        )

        val hourAngle = Math.toRadians((((hour % 12) + minute / 60f) * 30f - 90f).toDouble())
        val minAngle = Math.toRadians((minute * 6f - 90f).toDouble())

        canvas.drawLine(
            cx,
            cy,
            (cx + r * 0.52f * cos(hourAngle)).toFloat(),
            (cy + r * 0.52f * sin(hourAngle)).toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (darkText) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
                strokeWidth = sizePx * 0.045f
                strokeCap = Paint.Cap.ROUND
            }
        )
        canvas.drawLine(
            cx,
            cy,
            (cx + r * 0.76f * cos(minAngle)).toFloat(),
            (cy + r * 0.76f * sin(minAngle)).toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0EA5E9")
                strokeWidth = sizePx * 0.028f
                strokeCap = Paint.Cap.ROUND
            }
        )
        canvas.drawCircle(
            cx,
            cy,
            sizePx * 0.036f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (darkText) Color.parseColor("#0F172A") else Color.WHITE
            }
        )
        return bmp
    }

    /**
     * Pre-renders an illuminated moon disc matching current lunar phase.
     */
    fun renderMoonPhaseBitmap(
        illumination: Float,
        isWaxing: Boolean,
        darkText: Boolean,
        sizePx: Int = 200
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val r = sizePx * 0.34f

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx,
                cy,
                r * 1.45f,
                intArrayOf(
                    Color.argb(if (darkText) 55 else 85, 226, 232, 240),
                    Color.argb(0, 226, 232, 240)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * 1.45f, glowPaint)

        val shadowDisc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(46, 15, 23, 42) else Color.argb(56, 255, 255, 255)
        }
        canvas.drawCircle(cx, cy, r, shadowDisc)

        val litPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.parseColor("#1E293B") else Color.parseColor("#F8FAFC")
        }
        val save = canvas.save()
        val circleClip = Path().apply {
            addCircle(cx, cy, r, Path.Direction.CW)
        }
        canvas.clipPath(circleClip)

        val frac = illumination.coerceIn(0f, 1f)
        if (frac > 0.02f) {
            if (isWaxing) {
                canvas.drawRect(cx, cy - r, cx + r, cy + r, litPaint)
            } else {
                canvas.drawRect(cx - r, cy - r, cx, cy + r, litPaint)
            }
            val terminatorWidth = abs(1f - 2f * frac) * r
            val oval = RectF(cx - terminatorWidth, cy - r, cx + terminatorWidth, cy + r)
            if (frac >= 0.5f) {
                canvas.drawOval(oval, litPaint)
            } else {
                canvas.drawOval(oval, shadowDisc)
            }
        }
        canvas.restoreToCount(save)
        return bmp
    }

    /**
     * Pre-renders 3 concentric orbital progress arcs (Day, Month, Year).
     */
    fun renderOrbitalRingsBitmap(
        dayFraction: Float,
        monthFraction: Float,
        yearFraction: Float,
        darkText: Boolean,
        sizePx: Int = 220
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val stroke = sizePx * 0.072f
        val trackAlpha = if (darkText) 34 else 48
        val trackRgb = if (darkText) Color.argb(trackAlpha, 15, 23, 42) else Color.argb(trackAlpha, 255, 255, 255)

        val rings = listOf(
            Triple(sizePx * 0.41f, dayFraction.coerceIn(0.03f, 1f), Color.parseColor("#38BDF8")),
            Triple(sizePx * 0.30f, monthFraction.coerceIn(0.03f, 1f), Color.parseColor("#A78BFA")),
            Triple(sizePx * 0.19f, yearFraction.coerceIn(0.03f, 1f), Color.parseColor("#34D399"))
        )

        rings.forEach { (radius, frac, colorInt) ->
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = trackRgb
                style = Paint.Style.STROKE
                strokeWidth = stroke
            }
            canvas.drawCircle(cx, cy, radius, trackPaint)

            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorInt
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = stroke
            }
            val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawArc(rect, -90f, frac * 360f, false, arcPaint)
        }
        return bmp
    }

    /**
     * Pre-renders the Sun Trajectory Solar Horizon Arc (sunrise 06:00 -> sunset 18:00).
     */
    fun renderSolarHorizonArcBitmap(
        hour: Int,
        minute: Int,
        darkText: Boolean,
        widthPx: Int = 340,
        heightPx: Int = 140
    ): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val horizonY = heightPx * 0.78f
        val leftX = widthPx * 0.12f
        val rightX = widthPx * 0.88f
        val arcRx = (rightX - leftX) / 2f
        val arcRy = heightPx * 0.56f
        val cx = (leftX + rightX) / 2f

        // Horizon baseline
        val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(60, 15, 23, 42) else Color.argb(75, 255, 255, 255)
            strokeWidth = 2.5f
        }
        canvas.drawLine(widthPx * 0.06f, horizonY, widthPx * 0.94f, horizonY, horizonPaint)

        val arcRect = RectF(cx - arcRx, horizonY - arcRy, cx + arcRx, horizonY + arcRy)

        // Dashed full day arc
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(70, 15, 23, 42) else Color.argb(85, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
        }
        canvas.drawArc(arcRect, 180f, 180f, false, trackPaint)

        // Daylight progress (06:00 = 360m to 18:00 = 1080m)
        val totalMins = hour * 60 + minute
        val dayFrac = ((totalMins - 360f) / 720f).coerceIn(0f, 1f)
        val activeSweep = dayFrac * 180f

        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 6f
        }
        if (activeSweep > 2f) {
            canvas.drawArc(arcRect, 180f, activeSweep, false, activePaint)
        }

        // Sun orb position on the ellipse
        val sunAngleRad = Math.toRadians((180f + activeSweep).toDouble())
        val sunX = (cx + arcRx * cos(sunAngleRad)).toFloat()
        val sunY = (horizonY + arcRy * sin(sunAngleRad)).toFloat()

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                sunX,
                sunY,
                22f,
                intArrayOf(Color.argb(145, 251, 191, 36), Color.argb(0, 251, 191, 36)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(sunX, sunY, 22f, glowPaint)

        val sunCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
        }
        canvas.drawCircle(sunX, sunY, 8.5f, sunCorePaint)
        return bmp
    }

    /**
     * Pre-renders a Nothing-inspired 5x3 LED dot-matrix digital clock bitmap.
     */
    fun renderDotMatrixClockBitmap(
        hour: Int,
        minute: Int,
        darkText: Boolean,
        widthPx: Int = 340,
        heightPx: Int = 110
    ): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val timeText = String.format("%02d:%02d", hour, minute)
        // 5 chars * 3 cols + 4 spaces = 19 columns, 5 rows
        val totalCols = 19
        val totalRows = 5
        val cellSize = min(widthPx / (totalCols + 4f), heightPx / (totalRows + 2.5f))
        val dotRadius = cellSize * 0.36f

        val gridWidth = totalCols * cellSize
        val gridHeight = totalRows * cellSize
        val startX = (widthPx - gridWidth) / 2f + cellSize / 2f
        val startY = (heightPx - gridHeight) / 2f + cellSize / 2f

        val litPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
        }
        val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(26, 15, 23, 42) else Color.argb(32, 255, 255, 255)
        }

        var colOffset = 0
        timeText.forEach { ch ->
            val glyph = DOT_MATRIX_GLYPHS[ch] ?: DOT_MATRIX_GLYPHS['0']!!
            for (r in 0 until 5) {
                val rowMask = glyph[r]
                for (c in 0 until 3) {
                    val isOn = ((rowMask shr (2 - c)) and 1) == 1
                    val cx = startX + (colOffset + c) * cellSize
                    val cy = startY + r * cellSize
                    val paint = when {
                        !isOn -> dimPaint
                        ch == ':' -> accentPaint
                        else -> litPaint
                    }
                    canvas.drawCircle(cx, cy, dotRadius, paint)
                }
            }
            colOffset += 4
        }
        return bmp
    }

    /**
     * Pre-renders a 4x4 Binary Coded Decimal (BCD) matrix for H1, H2, M1, M2 digits.
     */
    fun renderBinaryClockMatrixBitmap(
        hour: Int,
        minute: Int,
        darkText: Boolean,
        sizePx: Int = 180
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val digits = intArrayOf(hour / 10, hour % 10, minute / 10, minute % 10)
        val cols = 4
        val rows = 4
        val step = sizePx / 5.4f
        val startX = (sizePx - (cols - 1) * step) / 2f
        val startY = (sizePx - (rows - 1) * step) / 2f
        val radius = step * 0.28f

        val onHourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
        }
        val onMinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
        }
        val offPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(38, 15, 23, 42) else Color.argb(46, 255, 255, 255)
        }

        for (c in 0 until cols) {
            val d = digits[c]
            for (r in 0 until rows) {
                val bitWeight = 1 shl (3 - r) // 8, 4, 2, 1 from top to bottom
                val isLit = (d and bitWeight) != 0
                val cx = startX + c * step
                val cy = startY + r * step
                val paint = when {
                    !isLit -> offPaint
                    c < 2 -> onHourPaint
                    else -> onMinPaint
                }
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
        return bmp
    }

    /**
     * Pre-renders a clean 7-column Monthly Calendar Grid with today's date highlighted in a pill.
     */
    fun renderMonthCalendarGridBitmap(
        date: LocalDate,
        darkText: Boolean,
        widthPx: Int = 340,
        heightPx: Int = 210
    ): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val headers = arrayOf("M", "T", "W", "T", "F", "S", "S")
        val colWidth = widthPx / 7f
        val headerY = heightPx * 0.14f

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.argb(135, 15, 23, 42) else Color.argb(155, 248, 250, 252)
            textSize = heightPx * 0.095f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        for (c in 0 until 7) {
            canvas.drawText(headers[c], colWidth * (c + 0.5f), headerY, headerPaint)
        }

        val firstOfMonth = date.withDayOfMonth(1)
        val startCol = firstOfMonth.dayOfWeek.value - 1 // Monday = 0 .. Sunday = 6
        val daysInMonth = date.lengthOfMonth()
        val todayDom = date.dayOfMonth

        val rowStartY = heightPx * 0.30f
        val rowStepY = (heightPx * 0.66f) / 6f

        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkText) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
            textSize = heightPx * 0.095f
            textAlign = Paint.Align.CENTER
        }
        val todayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = heightPx * 0.095f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val todayCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0EA5E9")
        }

        for (day in 1..daysInMonth) {
            val slot = startCol + (day - 1)
            val col = slot % 7
            val row = slot / 7
            val cx = colWidth * (col + 0.5f)
            val cy = rowStartY + row * rowStepY

            if (day == todayDom) {
                canvas.drawCircle(cx, cy - heightPx * 0.033f, heightPx * 0.072f, todayCirclePaint)
                canvas.drawText(day.toString(), cx, cy, todayTextPaint)
            } else {
                canvas.drawText(day.toString(), cx, cy, dayPaint)
            }
        }
        return bmp
    }

    fun isDarkTextSurface(context: Context, config: WidgetConfig, isSystemDark: Boolean): Boolean {
        return when (config.themeMode) {
            ThemeMode.LIGHT -> true
            ThemeMode.DARK -> false
            ThemeMode.AUTO -> {
                if (config.customHexColor != null) {
                    val c = parseHexOrNull(config.customHexColor) ?: return true
                    val luminance = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0
                    luminance > 0.55
                } else if (config.tintPreset == GlassTintPreset.MATERIAL_YOU_AUTO) {
                    !isSystemDark
                } else {
                    config.tintPreset.prefersDarkText
                }
            }
        }
    }

    private fun resolveGlassBaseColor(
        context: Context,
        config: WidgetConfig,
        isSystemDark: Boolean
    ): Int {
        config.customHexColor?.let { hex ->
            parseHexOrNull(hex)?.let { return it }
        }
        if (config.themeMode == ThemeMode.DARK && config.tintPreset.prefersDarkText) {
            return Color.parseColor("#111827")
        }
        if (config.themeMode == ThemeMode.LIGHT && !config.tintPreset.prefersDarkText) {
            return Color.parseColor("#FFFFFF")
        }
        if (config.tintPreset == GlassTintPreset.MATERIAL_YOU_AUTO && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val resId = if (isSystemDark) {
                android.R.color.system_neutral1_800
            } else {
                android.R.color.system_accent1_100
            }
            return context.getColor(resId)
        }
        return config.tintPreset.baseColorHex.toInt()
    }

    fun parseHexOrNull(hex: String): Int? {
        val cleaned = hex.trim().removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        return runCatching { Color.parseColor("#$cleaned") }.getOrNull()
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxSide.toFloat()
        val targetW = (bitmap.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}
