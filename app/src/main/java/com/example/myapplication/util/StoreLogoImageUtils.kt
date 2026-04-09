package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Loads an image from [sourceUri], applies a **center square (1:1) crop**, writes JPEG to cache, returns a `file://` URI for upload.
 */
object StoreLogoImageUtils {

    private const val MAX_OUTPUT_EDGE_PX = 1024
    private const val JPEG_QUALITY = 92

    @WorkerThread
    fun cropCenterSquareToJpegFile(context: Context, sourceUri: Uri): Uri {
        val bitmap = loadBitmapDownsampled(context, sourceUri, MAX_OUTPUT_EDGE_PX * 2)
            ?: error("Could not read image")
        val square = centerSquareCrop(bitmap)
        if (square !== bitmap && !bitmap.isRecycled) bitmap.recycle()
        val scaled = if (square.width > MAX_OUTPUT_EDGE_PX) {
            val scaledBmp = Bitmap.createScaledBitmap(square, MAX_OUTPUT_EDGE_PX, MAX_OUTPUT_EDGE_PX, true)
            if (scaledBmp != square && !square.isRecycled) square.recycle()
            scaledBmp
        } else {
            square
        }
        val outFile = File.createTempFile("store_logo_", ".jpg", context.cacheDir)
        FileOutputStream(outFile).use { os ->
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, os)) {
                error("Could not compress logo")
            }
        }
        if (!scaled.isRecycled) scaled.recycle()
        return Uri.fromFile(outFile)
    }

    private fun loadBitmapDownsampled(context: Context, uri: Uri, maxEdge: Int): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val w = info.size.width
                val h = info.size.height
                val sample = maxSampleSize(w, h, maxEdge)
                if (sample > 1) {
                    val tw = max(1, w / sample)
                    val th = max(1, h / sample)
                    decoder.setTargetSize(tw, th)
                }
            }
        } else {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSizeLegacy(bounds, maxEdge, maxEdge)
            }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }
    }

    private fun calculateInSampleSizeLegacy(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
        val h = options.outHeight.coerceAtLeast(1)
        val w = options.outWidth.coerceAtLeast(1)
        var inSampleSize = 1
        if (h > reqH || w > reqW) {
            var halfH = h / 2
            var halfW = w / 2
            while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun maxSampleSize(width: Int, height: Int, maxEdge: Int): Int {
        val longest = max(width, height).coerceAtLeast(1)
        var sample = 1
        while (longest / sample > maxEdge) sample *= 2
        return sample
    }

    private fun centerSquareCrop(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val size = min(w, h)
        val x = (w - size) / 2
        val y = (h - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}
