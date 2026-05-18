package com.afterlight.madeproject.utils

import android.graphics.Bitmap

object QrCodeGenerator {
    // Minimal placeholder QR visual. Replace with ZXing encoder for production scans.
    fun generate(data: String, size: Int = 512): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val hash = data.hashCode()
        for (x in 0 until size) {
            for (y in 0 until size) {
                val bit = ((x / 16) xor (y / 16) xor hash) and 1
                bmp.setPixel(x, y, if (bit == 1) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bmp
    }
}
