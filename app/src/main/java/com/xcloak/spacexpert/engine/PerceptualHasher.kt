package com.xcloak.spacexpert.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import javax.inject.Inject

class PerceptualHasher @Inject constructor() {

    /**
     * Computes a 64-bit average hash (aHash) for the image at [path].
     * Returns null if the file can't be decoded as an image.
     */
    fun computeHash(path: String): Long? {
        val bitmap = decodeDownsampled(path, targetSize = 8) ?: return null
        val grayscale = IntArray(64)

        var index = 0
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                grayscale[index++] = gray
            }
        }
        bitmap.recycle()

        val average = grayscale.sum() / 64
        var hash = 0L
        for (i in 0 until 64) {
            if (grayscale[i] >= average) {
                hash = hash or (1L shl i)
            }
        }
        return hash
    }

    fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    private fun decodeDownsampled(path: String, targetSize: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            var sampleSize = 1
            while (options.outWidth / sampleSize > targetSize * 4) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = BitmapFactory.decodeFile(path, decodeOptions) ?: return null
            Bitmap.createScaledBitmap(decoded, targetSize, targetSize, true).also {
                if (it != decoded) decoded.recycle()
            }
        } catch (e: Exception) {
            null
        }
    }
}