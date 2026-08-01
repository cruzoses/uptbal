package com.uptbal.sace.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtil {

    fun decodeBase64(dataUri: String?): Bitmap? {
        if (dataUri.isNullOrBlank()) return null
        val b64 = if (dataUri.startsWith("data:")) dataUri.substringAfter(',') else dataUri
        return runCatching {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    fun decodificar(bytes: ByteArray, maxDim: Int = 1024): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDim) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: throw IllegalArgumentException("No se pudo decodificar la imagen")
    }

    fun comprimirYEncodear(bitmap: Bitmap, maxDim: Int = 200): String {
        val escala = escala(bitmap, maxDim)
        val out = ByteArrayOutputStream()
        escala.compress(Bitmap.CompressFormat.JPEG, 70, out)
        return "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun escala(src: Bitmap, maxDim: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w == 0 || h == 0) return src
        val mayor = maxOf(w, h)
        if (mayor <= maxDim) return src
        val ratio = maxDim.toFloat() / mayor
        val nw = (w * ratio).toInt().coerceAtLeast(1)
        val nh = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }
}
