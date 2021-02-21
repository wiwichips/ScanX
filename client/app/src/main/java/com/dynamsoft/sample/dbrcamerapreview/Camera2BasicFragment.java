/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynamsoft.sample.dbrcamerapreview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dynamsoft.dbr.EnumImagePixelFormat;
import com.dynamsoft.dbr.IntermediateResult;
import com.dynamsoft.dbr.LocalizationResult;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.sample.dbrcamerapreview.util.AutoFocusHelper;
import com.dynamsoft.sample.dbrcamerapreview.util.CameraConstants;
import com.dynamsoft.sample.dbrcamerapreview.util.CameraUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2BasicFragment extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    static final int[] gZoomRatio = {
            10, 100, 114, 132, 151, 174, 190, 210, 221, 230, 249, 268, 283, 295, 308, 322, 336
    };
    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2BasicFragment";
    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    static boolean bDecoding = false;
    private static TextView mResult;
    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private static final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x02:
                    mResult.setText((String) msg.obj);
                    break;
            }
        }
    };

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback
            = null;
    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    int w = 0;
    int h = 0;
    int mZoomState = 0; //0-original,1-zoom in,2-zoom hold
    int mZoomRatIndex = 0;
    private MainActivity mMainActivity;
    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;
    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    private HandlerThread mBackgroundThread2;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    private Handler mBackgroundHandler2;
    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;
    private Size outputPreviewSize;
    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;
    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;
    /**
     * Whether the current camera device supports auto focus or not.
     */
    private boolean mAutoFocusSupported = true;
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    private QRCodeView mQrView;
    private Rect mQrCropRect;
    private CameraCharacteristics mCameraCharacteristics;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            if (reader != null) {
                Image mImage = reader.acquireLatestImage();
                if (mImage != null) {

                    ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                    int nRowStride = mImage.getPlanes()[0].getRowStride();
                    int nPixelStride = mImage.getPlanes()[0].getPixelStride();

                    if (mBackgroundHandler2 != null && !bDecoding) {
                        bDecoding = true;
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        ImageData imageData = new ImageData(bytes, reader.getWidth(), reader.getHeight(), nRowStride * nPixelStride);
                        Message message = new Message();
                        message.what = 0x101;
                        message.obj = imageData;
                        mBackgroundHandler2.sendMessage(message);

                        //TODO:save preview datas to file --only for debug
                        //saveYuvDataToLocalFile(bytes,reader.getWidth(),reader.getHeight());
                        //end TODO
                    }
                    mImage.close();
                }


            }
        }


    };
    /**
     * Whether the camera is manual focusing now
     */
    private boolean mIsManualFocusing;
    /**
     * The current camera auto focus mode
     */
    private boolean mAutoFocus = true;
    private Rect mCropRegion;
    private MeteringRectangle[] mAFRegions = AutoFocusHelper.getZeroWeightRegion();
    private MeteringRectangle[] mAERegions = AutoFocusHelper.getZeroWeightRegion();
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static Camera2BasicFragment newInstance() {
        return new Camera2BasicFragment();
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainActivity = ((MainActivity) getActivity());
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mResult = view.findViewById(R.id.tv_result);
        mTextureView = view.findViewById(R.id.texture);
        mQrView = view.findViewById(R.id.qr_view);
        //mQrView.setVisibility(View.INVISIBLE);
        mTextureView.setGestureListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                mCameraCharacteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = mCameraCharacteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For video image captures, we use the suitable available size.
                Size[] sizeList = map.getOutputSizes(ImageFormat.YUV_420_888);
                for (Size size : sizeList
                ) {
                    if (size.getWidth() * size.getHeight() > 1000000)
                        continue;
                    else {
                        outputPreviewSize = size;
                        break;
                    }

                }
                mImageReader = ImageReader.newInstance(outputPreviewSize.getWidth(), outputPreviewSize.getHeight(),
                        ImageFormat.YUV_420_888, /*maxImages*/3);

                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, null);// mBackgroundHandler2);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, outputPreviewSize);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }


                // Check if the flash is supported.
                Boolean available = mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;

                checkFlashSupported();

                if (w == 0 || h == 0) {
                    w = mTextureView.getWidth();
                    h = mTextureView.getHeight();
                }
                //show the crop view
                int nQrViewW = w * 3 / 4, nQrViewH = h * 3 / 4;
                if (nQrViewH > nQrViewW) nQrViewH = nQrViewW;
                else nQrViewW = nQrViewH;

                int boxLeft = (w - nQrViewW) / 2;
                int boxTop = (h - nQrViewH) / 2;
                if (boxLeft >= 0 && boxTop >= 0) {
                    mQrCropRect = new Rect(boxLeft, boxTop, boxLeft + nQrViewW, boxTop + nQrViewH);
                    mQrView.reSetboxview(mQrCropRect.left, mQrCropRect.top, mQrCropRect.width(), mQrCropRect.height());
                }


                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link Camera2BasicFragment#mCameraId}.
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }


    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        mBackgroundThread2 = new HandlerThread("CameraBK2");
        mBackgroundThread2.start();
        mBackgroundHandler2 = new Handler(mBackgroundThread2.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg == null || msg.obj == null)
                    return;

                Message message = mHandler.obtainMessage();
                message.what = 0x02;

                ImageData imageData = (ImageData) msg.obj;
                try {
                    TextResult[] results = mMainActivity.getMainBarcdoeReader().decodeBuffer(imageData.mBytes, imageData.mWidth, imageData.mHeight, imageData.mStride, EnumImagePixelFormat.IPF_NV21, "");
                    boolean bTempCropContains = false;
                    if (mMainActivity.getMainBarcdoeReader().getIntermediateResults().length >= 1) {
                        IntermediateResult[] intermediateResults = mMainActivity.getMainBarcdoeReader().getIntermediateResults();
                        LocalizationResult[] localizationResults = (LocalizationResult[]) intermediateResults[0].results;
                        for (LocalizationResult result : localizationResults) {
                            com.dynamsoft.dbr.Point[] points = result.resultPoints;
                            int leftX, leftY, rightX, rightY;
                            rightX = leftX = points[0].x;
                            rightY = leftY = points[0].y;
                            for (com.dynamsoft.dbr.Point pt : points) {
                                if (pt.x < leftX) leftX = pt.x;
                                if (pt.y < leftY) leftY = pt.y;
                                if (pt.x > rightX) rightX = pt.x;
                                if (pt.y > rightY) rightY = pt.y;
                            }
                            Rect frameRegion = new Rect(leftX, leftY, rightX, rightY);
                            Rect frameSize = new Rect(0, 0, outputPreviewSize.getWidth(), outputPreviewSize.getHeight());
                            Rect viewRegion = CameraUtil.ConvertFrameRegionToViewRegion(frameRegion, frameSize, CameraUtil.getOrientationDisplayOffset(getActivity(), mSensorOrientation), new Size(mTextureView.getWidth(), mTextureView.getHeight()));

                            if (mQrCropRect.contains(viewRegion) || viewRegion.contains(mQrCropRect) || mQrCropRect.intersect(mQrCropRect)) {
                                if (mQrCropRect.contains(viewRegion)) {
                                    mZoomState = 1; //zoom in
                                } else {
                                    mZoomState = 2;//zoom hold
                                }
                                bTempCropContains = true;
                                break;
                            }
                        }
                    }
                    if (!bTempCropContains) {
                        mZoomState = 0;//zoom original
                    }
                    if (results != null && results.length > 0) {
                        String str = paraseResult(results);
                        message.obj = str;
                        mHandler.sendMessage(message);
                    } else {
                        message.obj = "";
                        mHandler.sendMessage(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("BarcodeReaderException", e.getMessage());
                    message.obj = "";
                    mHandler.sendMessage(message);
                } finally {
                    bDecoding = false;
                }
                super.handleMessage(msg);
            }
        };

    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBackgroundThread2.quitSafely();
        try {
            mBackgroundThread2.join();
            mBackgroundThread2 = null;
            mBackgroundHandler2 = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            //todo:set the auto focus region and auto exposure region
            int width = mPreviewSize.getWidth() * 3 / 4;
            int height = mPreviewSize.getHeight() * 3 / 4;
            int boxWidth = (width < height) ? width : height;
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(new Point((mPreviewSize.getWidth() - boxWidth) / 2
                    , (mPreviewSize.getHeight() - boxWidth) / 2), new Size(boxWidth, boxWidth), 1000)});
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(new Point((mPreviewSize.getWidth() - boxWidth) / 2
                    , (mPreviewSize.getHeight() - boxWidth) / 2), new Size(boxWidth, boxWidth), 1000)});

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Check if the flash is supported.
     */
    private void checkFlashSupported() {
        Boolean available = mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        mFlashSupported = available == null ? false : available;
    }

    String paraseResult(TextResult[] result) {
        String strResult = "";
        if (result == null || result.length == 0) {
            return strResult;
        }

        for (int i = 0; i < result.length; i++) {
            String strCurResult = "[" + i + "] Barcode format:";
            if (result[i].barcodeFormat != 0) {
                strCurResult += result[i].barcodeFormatString;
            } else {
                strCurResult += result[i].barcodeFormatString_2;
            }
            strCurResult += "\n" + result[i].barcodeText;
            if (i == 0)
                strResult = strCurResult;
            else
                strResult += "\n\n" + strCurResult;

        }
        return strResult;
    }

    void saveYuvDataToLocalFile(byte[] data, int width, int height) {
        FileOutputStream output = null;
        try {
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
            Bitmap bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
            File mFile = new File(getPictureFilePath(getActivity()));

            output = new FileOutputStream(mFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, output);
            output.write(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getPictureFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".jpg";
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    private class ImageData {
        private final int mWidth;
        private final int mHeight;
        private final int mStride;
        byte[] mBytes;

        ImageData(byte[] bytes, int nWidth, int nHeight, int nStride) {
            mBytes = bytes;
            mWidth = nWidth;
            mHeight = nHeight;
            mStride = nStride;
        }
    }

}
