package com.lumaglass.widgets.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object BitmapHelper {
    private const val PHOTO_DIR_NAME = "widget_photos"
    private const val MAX_WIDGET_IMAGE_DIM = 512

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
     * tint preset (or Material You / Pro custom hex), and 1dp specular border.
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
        val topAlpha = (alphaInt * 1.08f).roundToInt().coerceIn(0, 250)
        val bottomAlpha = (alphaInt * 0.82f).roundToInt().coerceIn(0, 240)

        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)

        val startColor = Color.argb(topAlpha, min(255, r + 14), min(255, g + 14), min(255, b + 14))
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

        // Crisp 1dp specular border
        val borderAlpha = if (config.opacity < 0.1f) 110 else 145
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkTextSurface(context, config, isSystemDark)) {
                Color.argb(borderAlpha, 255, 255, 255)
            } else {
                Color.argb((borderAlpha * 0.6f).roundToInt(), 255, 255, 255)
            }
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(rect, radiusPx, radiusPx, borderPaint)

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
        if (config.themeMode == ThemeMode.DARK && config.tintPreset == GlassTintPreset.FROSTED_WHITE) {
            return Color.parseColor("#111827")
        }
        if (config.themeMode == ThemeMode.LIGHT && config.tintPreset == GlassTintPreset.SMOKED_DARK) {
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
