package com.example.myapplication

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var scaleGestureDetector: ScaleGestureDetector
    private val matrix = Matrix()
    private var scaleFactor = 1.0f

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        scaleType = ScaleType.MATRIX
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        fitToScreen()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private fun fitToScreen() {
        val drawable = drawable ?: return
        val drawableRect = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)
        imageMatrix = matrix
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f))
            matrix.postScale(detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusY)
            imageMatrix = matrix
            return true
        }
    }
}