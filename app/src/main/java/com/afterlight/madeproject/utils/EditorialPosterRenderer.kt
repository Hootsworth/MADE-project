package com.afterlight.madeproject.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color

object EditorialPosterRenderer {

    fun renderPoster(
        title: String,
        subtitle: String,
        accent: Color,
        width: Int = 1080,
        height: Int = 1440
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFF5F3EE.toInt())

        val stripePaint = Paint().apply { color = accent.toArgbCompat() }
        canvas.drawRect(Rect(80, 80, width - 80, 120), stripePaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1F1D1A.toInt()
            textSize = 86f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF48443D.toInt()
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        canvas.drawText(title.take(30), 80f, height / 2f, titlePaint)
        canvas.drawText(subtitle.take(50), 80f, height / 2f + 80f, subtitlePaint)
        return bitmap
    }

    private fun Color.toArgbCompat(): Int {
        val a = (alpha * 255).toInt()
        val r = (red * 255).toInt()
        val g = (green * 255).toInt()
        val b = (blue * 255).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
