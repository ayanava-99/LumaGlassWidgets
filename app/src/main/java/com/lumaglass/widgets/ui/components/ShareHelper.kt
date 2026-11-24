package com.lumaglass.widgets.ui.components

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.lumaglass.widgets.data.BitmapHelper
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.ui.theme.WallpaperBackdrop
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun playStoreUrl(context: Context): String =
        "https://play.google.com/store/apps/details?id=${context.packageName}"

    fun shareAppLink(context: Context) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "LumaGlass – Aesthetic Liquid Glass Widgets")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out LumaGlass Widgets — 40 frosted Liquid Glass & Material You home screen widgets (100% offline):\n${playStoreUrl(context)}"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share LumaGlass"))
    }

    fun openPlayStoreListing(context: Context) {
        val pkg = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(marketIntent)
        }.onFailure {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl(context))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(webIntent) }
        }
    }

    fun shareWidgetShowcaseCard(
        context: Context,
        config: WidgetConfig,
        backdrop: WallpaperBackdrop
    ) {
        runCatching {
            val width = 1080
            val height = 1080
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            val bgColors = backdrop.colors.map { it.toArgb() }.toIntArray()
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    bgColors, null, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val isSysDark = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val darkText = BitmapHelper.isDarkTextSurface(context, config, isSysDark)
            val cardBmp = BitmapHelper.renderGlassSurfaceBitmap(
                context = context,
                config = config,
                isSystemDark = isSysDark,
                widthPx = 760,
                heightPx = 540
            )
            val cardLeft = (width - cardBmp.width) / 2f
            val cardTop = 210f
            canvas.drawBitmap(cardBmp, cardLeft, cardTop, null)
            cardBmp.recycle()

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (darkText) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
                textSize = 46f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(config.templateId.title, width / 2f, cardTop + 250f, titlePaint)

            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (darkText) Color.parseColor("#475569") else Color.parseColor("#CBD5E1")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "${config.tintPreset.label} • ${config.surfaceTexture.label}",
                width / 2f,
                cardTop + 310f,
                subPaint
            )

            // Footer branding pill
            val pillRect = RectF(220f, 870f, 860f, 955f)
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 15, 23, 42)
            }
            canvas.drawRoundRect(pillRect, 48f, 48f, pillPaint)

            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val bounds = Rect()
            val brandText = "Made with LumaGlass Widgets"
            brandPaint.getTextBounds(brandText, 0, brandText.length, bounds)
            canvas.drawText(brandText, width / 2f, pillRect.centerY() + bounds.height() / 2f, brandPaint)

            val cacheDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val outFile = File(cacheDir, "lumaglass_setup_card.png")
            FileOutputStream(outFile).use { fos ->
                bmp.compress(Bitmap.CompressFormat.PNG, 95, fos)
            }
            bmp.recycle()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "My ${config.templateId.title} setup made with LumaGlass Widgets:\n${playStoreUrl(context)}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Setup Card"))
        }
    }
}
