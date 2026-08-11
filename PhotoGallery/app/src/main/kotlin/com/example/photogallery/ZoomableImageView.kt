package com.example.photogallery

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val matrixValues = FloatArray(9)
    private val imageMatrix2 = Matrix()

    private var minScale = 1f
    private var maxScale = 5f
    private var currentScale = 1f

    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var isDragging = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val newScale = currentScale * detector.scaleFactor
            val clamped = max(minScale, min(newScale, maxScale))
            val factor = clamped / currentScale
            currentScale = clamped
            imageMatrix2.postScale(factor, factor, detector.focusX, detector.focusY)
            imageMatrix = imageMatrix2
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (currentScale > minScale + 0.01f) {
                resetZoom()
            } else {
                currentScale = maxScale / 2f
                imageMatrix2.postScale(currentScale, currentScale, e.x, e.y)
                imageMatrix = imageMatrix2
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            performClick()
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastFocusX = event.x
                lastFocusY = event.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentScale > minScale + 0.01f && event.pointerCount == 1) {
                    val dx = event.x - lastFocusX
                    val dy = event.y - lastFocusY
                    imageMatrix2.postTranslate(dx, dy)
                    imageMatrix = imageMatrix2
                    lastFocusX = event.x
                    lastFocusY = event.y
                    isDragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun resetZoom() {
        currentScale = minScale
        centerImage()
    }

    private fun centerImage() {
        val d = drawable ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val drawableWidth = d.intrinsicWidth.toFloat()
        val drawableHeight = d.intrinsicHeight.toFloat()

        if (drawableWidth == 0f || drawableHeight == 0f || viewWidth == 0f || viewHeight == 0f) return

        val scale = min(viewWidth / drawableWidth, viewHeight / drawableHeight)
        minScale = scale
        currentScale = scale

        val dx = (viewWidth - drawableWidth * scale) / 2f
        val dy = (viewHeight - drawableHeight * scale) / 2f

        imageMatrix2.reset()
        imageMatrix2.postScale(scale, scale)
        imageMatrix2.postTranslate(dx, dy)
        imageMatrix = imageMatrix2
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        post { centerImage() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerImage()
    }
}
