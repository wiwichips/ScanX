package com.dynamsoft.sample.dbrcamerapreview.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class CameraUtil {
    /**
     * Clamps x to between min and max (inclusive on both ends, x = min --> min,
     * x = max --> max).
     */
    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    /**
     * Clamps x to between min and max (inclusive on both ends, x = min --> min,
     * x = max --> max).
     */
    public static float clamp(float x, float min, float max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static void inlineRectToRectF(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static Rect rectFToRect(RectF rectF) {
        Rect rect = new Rect();
        inlineRectToRectF(rectF, rect);
        return rect;
    }

    public static RectF rectToRectF(Rect r) {
        return new RectF(r.left, r.top, r.right, r.bottom);
    }

    /**
     * Linear interpolation between a and b by the fraction t. t = 0 --> a, t =
     * 1 --> b.
     */
    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Given (nx, ny) \in [0, 1]^2, in the display's portrait coordinate system,
     * returns normalized sensor coordinates \in [0, 1]^2 depending on how the
     * sensor's orientation \in {0, 90, 180, 270}.
     * <p>
     * Returns null if sensorOrientation is not one of the above.
     * </p>
     */
    public static PointF normalizedSensorCoordsForNormalizedDisplayCoords(
            float nx, float ny, int sensorOrientation) {
        switch (sensorOrientation) {
            case 0:
                return new PointF(nx, ny);
            case 90:
                return new PointF(ny, 1.0f - nx);
            case 180:
                return new PointF(1.0f - nx, 1.0f - ny);
            case 270:
                return new PointF(1.0f - ny, nx);
            default:
                return null;
        }
    }

    public static Rect boundaryRotate(Point orgPt, Rect rect, boolean bLeft) {
        float orgx = orgPt.x;
        float orgy = orgPt.y;

        float rotatex = orgy;
        float rotatey = orgx;
        float[] currentBoundary = new float[8];
        currentBoundary[0] = rect.left;
        currentBoundary[1] = rect.top;

        currentBoundary[2] = rect.right;
        currentBoundary[3] = rect.top;

        currentBoundary[4] = rect.right;
        currentBoundary[5] = rect.bottom;

        currentBoundary[6] = rect.left;
        currentBoundary[7] = rect.bottom;

        int[] rotateBoundary = new int[8];
        if (bLeft) {
            rotateBoundary[6] = (int) ((currentBoundary[0] - orgx) * Math.cos(Math.PI * 0.5f) + (currentBoundary[1] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[7] = (int) (-(currentBoundary[0] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[1] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);

            rotateBoundary[0] = (int) ((currentBoundary[2] - orgx) * Math.cos(Math.PI * 0.5f) + (currentBoundary[3] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[1] = (int) (-(currentBoundary[2] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[3] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);

            rotateBoundary[2] = (int) ((currentBoundary[4] - orgx) * Math.cos(Math.PI * 0.5f) + (currentBoundary[5] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[3] = (int) (-(currentBoundary[4] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[5] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);

            rotateBoundary[4] = (int) ((currentBoundary[6] - orgx) * Math.cos(Math.PI * 0.5f) + (currentBoundary[7] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[5] = (int) (-(currentBoundary[6] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[7] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);
        } else {
            rotateBoundary[2] = (int) ((currentBoundary[0] - orgx) * Math.cos(Math.PI * 0.5f) - (currentBoundary[1] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[3] = (int) (((currentBoundary[0] - orgx) * Math.sin(Math.PI * 0.5f)) + ((currentBoundary[1] - orgy) * Math.cos(Math.PI * 0.5f)) + rotatey);

            rotateBoundary[4] = (int) ((currentBoundary[2] - orgx) * Math.cos(Math.PI * 0.5f) - (currentBoundary[3] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[5] = (int) ((currentBoundary[2] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[3] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);

            rotateBoundary[6] = (int) ((currentBoundary[4] - orgx) * Math.cos(Math.PI * 0.5f) - (currentBoundary[5] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[7] = (int) ((currentBoundary[4] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[5] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);

            rotateBoundary[0] = (int) ((currentBoundary[6] - orgx) * Math.cos(Math.PI * 0.5f) - (currentBoundary[7] - orgy) * Math.sin(Math.PI * 0.5f) + rotatex);
            rotateBoundary[1] = (int) ((currentBoundary[6] - orgx) * Math.sin(Math.PI * 0.5f) + (currentBoundary[7] - orgy) * Math.cos(Math.PI * 0.5f) + rotatey);
        }

        Rect rotateRect = new Rect(rotateBoundary[0], rotateBoundary[1], rotateBoundary[2], rotateBoundary[5]);

        return rotateRect;
    }

    public static Rect boundaryRotate180(Point orgPt, Rect rect) {
        float orgx = orgPt.x;
        float orgy = orgPt.y;

        float rotatex = orgy;
        float rotatey = orgx;
        float[] currentBoundary = new float[8];
        currentBoundary[0] = rect.left;
        currentBoundary[1] = rect.top;

        currentBoundary[2] = rect.right;
        currentBoundary[3] = rect.top;

        currentBoundary[4] = rect.right;
        currentBoundary[5] = rect.bottom;

        currentBoundary[6] = rect.left;
        currentBoundary[7] = rect.bottom;
        int[] rotateBoundary = new int[8];
        rotateBoundary[4] = (int) (orgx - (currentBoundary[0] - orgx));
        rotateBoundary[5] = (int) (orgy - (currentBoundary[1] - orgy));

        rotateBoundary[6] = (int) (orgx - (currentBoundary[2] - orgx));
        rotateBoundary[7] = (int) (orgy - (currentBoundary[3] - orgy));

        rotateBoundary[0] = (int) (orgx - (currentBoundary[4] - orgx));
        rotateBoundary[1] = (int) (orgy - (currentBoundary[5] - orgy));

        rotateBoundary[2] = (int) (orgx - (currentBoundary[6] - orgx));
        rotateBoundary[3] = (int) (orgy - (currentBoundary[7] - orgy));
        Rect rotateRect = new Rect(rotateBoundary[0], rotateBoundary[1], rotateBoundary[2], rotateBoundary[5]);

        return rotateRect;
    }

    public static int getOrientationDisplayOffset(Context context, int nSensorOrientation) {
        int mDisplayOffset = 0;
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                mDisplayOffset = 0;
                break;
            case Surface.ROTATION_90:
                mDisplayOffset = 90;
                break;
            case Surface.ROTATION_180:
                mDisplayOffset = 180;
                break;
            case Surface.ROTATION_270:
                mDisplayOffset = 270;
                break;
            default:
                mDisplayOffset = 0;
                break;
        }
        //		if (mFacing == Facing.FRONT) {
//			// Here we had ((mSensorOffset - mDisplayOffset) + 360 + 180) % 360
//			// And it seemed to give the same results for various combinations, but not for all (e.g. 0 - 270).
//			return (360 - ((mSensorOffset + mDisplayOffset) % 360)) % 360;
//		} else
        {
            int nOrientationDisplayOffset = (nSensorOrientation - mDisplayOffset + 360) % 360;
            return nOrientationDisplayOffset;
        }
    }

    public static Rect ConvertViewRegionToVideoFrameRegion(Rect viewRegion, Rect frameSize, int nOrientationDisplayOffset, Size szCameraView) {
        Rect convertRegion;
        final int rotateDegree = nOrientationDisplayOffset;
        if (rotateDegree == 90) {
            convertRegion = boundaryRotate(new Point(szCameraView.getWidth() / 2, szCameraView.getHeight() / 2), viewRegion, true);
        } else if (rotateDegree == 180) {
            convertRegion = boundaryRotate180(new Point(szCameraView.getWidth() / 2, szCameraView.getHeight() / 2), viewRegion);
        } else if (nOrientationDisplayOffset == 270) {
            convertRegion = boundaryRotate(new Point(szCameraView.getWidth() / 2, szCameraView.getHeight() / 2), viewRegion, false);
        } else {
            convertRegion = viewRegion;
        }

        int nViewW = szCameraView.getWidth();
        int nViewH = szCameraView.getHeight();
        float fScaleH = (nOrientationDisplayOffset % 180 == 0) ? 1.0f * frameSize.height() / nViewH : 1.0f * frameSize.height() / nViewW;
        float fScaleW = (nOrientationDisplayOffset % 180 == 0) ? 1.0f * frameSize.width() / nViewW : 1.0f * frameSize.width() / nViewH;
        float fScale = (fScaleH > fScaleW) ? fScaleW : fScaleH;
        int boxLeft = (int) (convertRegion.left * fScale);
        int boxTop = (int) (convertRegion.top * fScale);
        int boxWidth = (int) (convertRegion.width() * fScale);
        int boxHeight = (int) (convertRegion.height() * fScale);
        Rect frameRegion = new Rect(boxLeft, boxTop, boxWidth + boxLeft, boxTop + boxHeight);
        return frameRegion;
    }

    public static Rect ConvertFrameRegionToViewRegion(Rect frameRegion, Rect frameSize, int nOrientationDisplayOffset, Size szCameraView) {

        Rect imageRect = frameSize;
        Rect roateRect = frameRegion;
        int rotateDegree = nOrientationDisplayOffset;

        if (rotateDegree == 90) {
            roateRect = boundaryRotate(new Point(imageRect.width() / 2, imageRect.height() / 2), frameRegion, false);
        } else if (rotateDegree == 180) {
            roateRect = boundaryRotate180(new Point(imageRect.width() / 2, imageRect.height() / 2), frameRegion);
        } else if (nOrientationDisplayOffset == 270) {
            roateRect = boundaryRotate(new Point(imageRect.width() / 2, imageRect.height() / 2), frameRegion, true);
        }

        int nViewW = szCameraView.getWidth();
        int nViewH = szCameraView.getHeight();

        float fScaleH = (nOrientationDisplayOffset % 180 == 0) ? 1.0f * nViewH / imageRect.height() : 1.0f * nViewH / imageRect.width();
        float fScaleW = (nOrientationDisplayOffset % 180 == 0) ? 1.0f * nViewW / imageRect.width() : 1.0f * nViewW / imageRect.height();
        float fScale = (fScaleH > fScaleW) ? fScaleW : fScaleH;

        int boxLeft = (int) (roateRect.left * fScale);
        int boxTop = (int) (roateRect.top * fScale);
        int boxWidth = (int) (roateRect.width() * fScale);
        int boxHeight = (int) (roateRect.height() * fScale);
        Rect viewRegion = new Rect(boxLeft, boxTop, boxWidth + boxLeft, boxTop + boxHeight);
        return viewRegion;

    }
}
