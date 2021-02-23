package com.dynamsoft.sample.dbrcamerapreview.util

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import java.lang.IllegalArgumentException
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object CameraUtil {
    /**
     * Clamps x to between min and max (inclusive on both ends, x = min --> min, x = max --> max).
     */
    fun <T> clamp(x: T, min: T, max: T): T where T: Number, T: Comparable<T> {
        return when {
            x > max -> max
            x < min -> min
            else -> x
        }
    }

    fun inlineRectToRectF(rectF: RectF, rect: Rect) {
        rect.left = rectF.left.roundToInt()
        rect.top = rectF.top.roundToInt()
        rect.right = rectF.right.roundToInt()
        rect.bottom = rectF.bottom.roundToInt()
    }

    fun rectFToRect(rectF: RectF): Rect {
        val rect = Rect()
        inlineRectToRectF(rectF, rect)
        return rect
    }

    fun rectToRectF(r: Rect): RectF {
        return RectF(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat())
    }

    /**
     * Linear interpolation between a and b by the fraction t.
     * t = 0 --> a, t = 1 --> b.
     */
    fun linearInterpolation(a: Float, b: Float, t: Float): Float {
        return a + t * (b - a)
    }

    /**
     * Given (nx, ny) \in [0, 1]^2, in the display's portrait coordinate system,
     * returns normalized sensor coordinates \in [0, 1]^2 depending on how the
     * sensor's orientation \in {0, 90, 180, 270}.
     *
     * Returns null if sensorOrientation is not one of the above.
     *
     */
    fun normalizedSensorCoordsForNormalizedDisplayCoords(nx: Float, ny: Float, sensorOrientation: Int): PointF {
        return when (sensorOrientation) {
            0 -> PointF(nx, ny)
            90 -> PointF(ny, 1.0f - nx)
            180 -> PointF(1.0f - nx, 1.0f - ny)
            270 -> PointF(1.0f - ny, nx)
            else -> throw IllegalArgumentException("Invalid sensorOrientation: $sensorOrientation.")
        }
    }

    private fun getBoundary(rect: Rect): FloatArray {
        val boundary = FloatArray(8)
        boundary[0] = rect.left.toFloat()
        boundary[1] = rect.top.toFloat()
        boundary[2] = rect.right.toFloat()
        boundary[3] = rect.top.toFloat()
        boundary[4] = rect.right.toFloat()
        boundary[5] = rect.bottom.toFloat()
        boundary[6] = rect.left.toFloat()
        boundary[7] = rect.bottom.toFloat()
        return boundary
    }

    fun boundaryRotate(orgPt: Point, rect: Rect, bLeft: Boolean): Rect {
        val orgX = orgPt.x.toFloat()
        val orgY = orgPt.y.toFloat()
        val currentBoundary: FloatArray = getBoundary(rect)
        val rotateBoundary = IntArray(8)
        val cosHalfPi = cos(Math.PI * 0.5f)
        val sinHalfPi = sin(Math.PI * 0.5f)

        if (bLeft) {
            rotateBoundary[6] = ((currentBoundary[0] - orgX) * cosHalfPi + (currentBoundary[1] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[7] = (-(currentBoundary[0] - orgX) * sinHalfPi + (currentBoundary[1] - orgY) * cosHalfPi + orgX).toInt()
            rotateBoundary[0] = ((currentBoundary[2] - orgX) * cosHalfPi + (currentBoundary[3] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[1] = (-(currentBoundary[2] - orgX) * sinHalfPi + (currentBoundary[3] - orgY) * cosHalfPi + orgX).toInt()
            rotateBoundary[2] = ((currentBoundary[4] - orgX) * cosHalfPi + (currentBoundary[5] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[3] = (-(currentBoundary[4] - orgX) * sinHalfPi + (currentBoundary[5] - orgY) * cosHalfPi + orgX).toInt()
            rotateBoundary[4] = ((currentBoundary[6] - orgX) * cosHalfPi + (currentBoundary[7] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[5] = (-(currentBoundary[6] - orgX) * sinHalfPi + (currentBoundary[7] - orgY) * cosHalfPi + orgX).toInt()
        } else {
            rotateBoundary[2] = ((currentBoundary[0] - orgX) * cosHalfPi - (currentBoundary[1] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[3] = ((currentBoundary[0] - orgX) * sinHalfPi + (currentBoundary[1] - orgY) * cosHalfPi + orgX).toInt()
            rotateBoundary[4] = ((currentBoundary[2] - orgX) * cosHalfPi - (currentBoundary[3] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[5] = ((currentBoundary[2] - orgX) * sinHalfPi + (currentBoundary[3] - orgY) * cosHalfPi + orgX).toInt()
            rotateBoundary[6] = ((currentBoundary[4] - orgX) * cosHalfPi - (currentBoundary[5] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[7] = ((currentBoundary[4] - orgX) * sinHalfPi + (currentBoundary[5] - orgY) * cosHalfPi + orgX).toInt()
            rotateBoundary[0] = ((currentBoundary[6] - orgX) * cosHalfPi - (currentBoundary[7] - orgY) * sinHalfPi + orgY).toInt()
            rotateBoundary[1] = ((currentBoundary[6] - orgX) * sinHalfPi + (currentBoundary[7] - orgY) * cosHalfPi + orgX).toInt()
        }

        return Rect(rotateBoundary[0], rotateBoundary[1], rotateBoundary[2], rotateBoundary[5])
    }

    fun boundaryRotate180(orgPt: Point, rect: Rect): Rect {
        val orgX = orgPt.x.toFloat()
        val orgY = orgPt.y.toFloat()
        val currentBoundary: FloatArray = getBoundary(rect)
        val rotateBoundary = IntArray(8)

        rotateBoundary[4] = (orgX - (currentBoundary[0] - orgX)).toInt()
        rotateBoundary[5] = (orgY - (currentBoundary[1] - orgY)).toInt()
        rotateBoundary[6] = (orgX - (currentBoundary[2] - orgX)).toInt()
        rotateBoundary[7] = (orgY - (currentBoundary[3] - orgY)).toInt()
        rotateBoundary[0] = (orgX - (currentBoundary[4] - orgX)).toInt()
        rotateBoundary[1] = (orgY - (currentBoundary[5] - orgY)).toInt()
        rotateBoundary[2] = (orgX - (currentBoundary[6] - orgX)).toInt()
        rotateBoundary[3] = (orgY - (currentBoundary[7] - orgY)).toInt()

        return Rect(rotateBoundary[0], rotateBoundary[1], rotateBoundary[2], rotateBoundary[5])
    }

    fun getOrientationDisplayOffset(context: Context, nSensorOrientation: Int): Int {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val mDisplayOffset: Int = when (display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        return (nSensorOrientation - mDisplayOffset + 360) % 360
    }

    fun convertViewRegionToVideoFrameRegion(viewRegion: Rect, frameSize: Rect, nOrientationDisplayOffset: Int, szCameraView: Size): Rect {
        val convertRegion: Rect = when (nOrientationDisplayOffset) {
            90 -> boundaryRotate(Point(szCameraView.width / 2, szCameraView.height / 2), viewRegion, true)
            180 -> boundaryRotate180(Point(szCameraView.width / 2, szCameraView.height / 2), viewRegion)
            270 -> boundaryRotate(Point(szCameraView.width / 2, szCameraView.height / 2), viewRegion, false)
            else -> viewRegion
        }

        val nViewW = szCameraView.width
        val nViewH = szCameraView.height
        val fScaleH: Float
        val fScaleW: Float
        if (nOrientationDisplayOffset % 180 == 0) {
            fScaleH = 1.0f * frameSize.height() / nViewH
            fScaleW =  1.0f * frameSize.width() / nViewW
        } else {
            fScaleH = 1.0f * frameSize.height() / nViewW
            fScaleW = 1.0f * frameSize.width() / nViewH
        }

        val fScale = if (fScaleH > fScaleW) fScaleW else fScaleH
        val boxLeft = (convertRegion.left * fScale).toInt()
        val boxTop = (convertRegion.top * fScale).toInt()
        val boxWidth = (convertRegion.width() * fScale).toInt()
        val boxHeight = (convertRegion.height() * fScale).toInt()
        return Rect(boxLeft, boxTop, boxWidth + boxLeft, boxTop + boxHeight)
    }

    fun convertFrameRegionToViewRegion(frameRegion: Rect, frameSize: Rect, nOrientationDisplayOffset: Int, szCameraView: Size): Rect {
        var roateRect = frameRegion
        if (nOrientationDisplayOffset == 90) {
            roateRect = boundaryRotate(Point(frameSize.width() / 2, frameSize.height() / 2), frameRegion, false)
        } else if (nOrientationDisplayOffset == 180) {
            roateRect = boundaryRotate180(Point(frameSize.width() / 2, frameSize.height() / 2), frameRegion)
        } else if (nOrientationDisplayOffset == 270) {
            roateRect = boundaryRotate(Point(frameSize.width() / 2, frameSize.height() / 2), frameRegion, true)
        }
        val nViewW = szCameraView.width
        val nViewH = szCameraView.height
        val fScaleH = if (nOrientationDisplayOffset % 180 == 0) 1.0f * nViewH / frameSize.height() else 1.0f * nViewH / frameSize.width()
        val fScaleW = if (nOrientationDisplayOffset % 180 == 0) 1.0f * nViewW / frameSize.width() else 1.0f * nViewW / frameSize.height()
        val fScale = if (fScaleH > fScaleW) fScaleW else fScaleH
        val boxLeft = (roateRect.left * fScale).toInt()
        val boxTop = (roateRect.top * fScale).toInt()
        val boxWidth = (roateRect.width() * fScale).toInt()
        val boxHeight = (roateRect.height() * fScale).toInt()
        return Rect(boxLeft, boxTop, boxWidth + boxLeft, boxTop + boxHeight)
    }
}