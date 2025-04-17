package it.divitech.nliticketapp.helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImageUtils {
    @JvmStatic
    fun toMonochrome(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height

        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(original, 0f, 0f, paint)

        return grayscaleBitmap
    }
}
