/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mz.bancounico.uocr.logic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.StringDef;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.bancounico.uocr.processors.Processor;
import mz.bancounico.uocr.interfaces.AutoFocusCallback;
import mz.bancounico.uocr.interfaces.AutoFocusMoveCallback;
import mz.bancounico.uocr.interfaces.PictureCallback;
import mz.bancounico.uocr.interfaces.ShutterCallback;


@SuppressWarnings("deprecation")
public class CameraSource {
    @SuppressLint("InlinedApi")

    @StringDef({
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_AUTO,
            Camera.Parameters.FOCUS_MODE_EDOF,
            Camera.Parameters.FOCUS_MODE_FIXED,
            Camera.Parameters.FOCUS_MODE_INFINITY,
            Camera.Parameters.FOCUS_MODE_MACRO
    })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface FocusMode {
    }

    @StringDef({
            Camera.Parameters.FLASH_MODE_ON,
            Camera.Parameters.FLASH_MODE_OFF,
            Camera.Parameters.FLASH_MODE_AUTO,
            Camera.Parameters.FLASH_MODE_RED_EYE,
            Camera.Parameters.FLASH_MODE_TORCH
    })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface FlashMode {
    }


    protected final Object mLock = new Object();


    private static final String TAG = "OpenCameraSource";

    public static final int ROTATION_MODE_PORTRAIT = 0;
    public static final int ROTATION_MODE_LANDSCAPE = 1;

    protected int rotationMode;

    protected Handler ocrHandler;

    protected boolean pictureTaken = false;
    protected boolean barcodeDetected = false;

    protected SizePair sizePair;

    protected Camera camera;

    private static final int DUMMY_TEXTURE_NAME = 100;

    protected Processor processor;

    protected static Context mContext;

    private final Object mCameraLock = new Object();

    protected Camera mCamera;

    protected int mFacing;

    private static final int FOCUS_AREA_SIZE = 1;

    protected int mRotation;

    protected Size mPreviewSize;

    protected String mFocusMode = null;
    protected String mFlashMode = null;

    protected boolean canCaptureBarcodeImage = false;

    private SurfaceView mDummySurfaceView;
    private SurfaceTexture mDummySurfaceTexture;

    protected Thread mProcessingThread;
    protected FrameProcessingRunnable mFrameProcessor;

    protected Map<byte[], ByteBuffer> mBytesToByteBuffer = new HashMap<>();


    public void setPictureTaken(boolean pictureTaken) {
        this.pictureTaken = pictureTaken;
    }

    public void setBarcodeDetected(boolean barcodeDetected) {
        this.barcodeDetected = barcodeDetected;
    }

    public void release() {
        synchronized (mCameraLock) {
            stop();
            mFrameProcessor.release();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    public CameraSource start() throws IOException {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                return this;
            }
            mCamera = createCamera();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mDummySurfaceTexture = new SurfaceTexture(DUMMY_TEXTURE_NAME);
                mCamera.setPreviewTexture(mDummySurfaceTexture);
            } else {
                mDummySurfaceView = new SurfaceView(mContext);
                mCamera.setPreviewDisplay(mDummySurfaceView.getHolder());
            }
            mCamera.startPreview();


        }
        return this;
    }

    public void startProcessing() {

        mProcessingThread = new Thread(mFrameProcessor);
        mFrameProcessor.setActive(true);
        mProcessingThread.start();

    }

    public void stopProcessingThread() {
        mFrameProcessor.setActive(false);
        if (mProcessingThread != null) {
            try {

                mProcessingThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }
            mProcessingThread = null;
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    public CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                return this;
            }

            mCamera = createCamera();
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();

        }
        return this;
    }

    public void stop() {
        synchronized (mCameraLock) {

            mBytesToByteBuffer.clear();

            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mCamera.setPreviewTexture(null);

                    } else {
                        mCamera.setPreviewDisplay(null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear camera preview: " + e);
                }
                mCamera.release();
                mCamera = null;
            }
        }
    }


    public Size getPreviewSize() {
        return mPreviewSize;
    }


    public int getCameraFacing() {
        return mFacing;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void focusOnTouch(float rawX, float rawY) {
        if (mCamera != null) {

            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getMaxNumMeteringAreas() > 0) {
                    Log.i(TAG, "fancy !");
                    Rect rect = calculateFocusArea(rawX, rawY);

                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                    meteringAreas.add(new Camera.Area(rect, 800));
                    parameters.setFocusAreas(meteringAreas);

                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(mAutoFocusTakePictureCallback);
                } else {
                    mCamera.autoFocus(mAutoFocusTakePictureCallback);
                }
            } catch (RuntimeException r) {

            }

        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreviewSize.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreviewSize.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }



    public void takePicture(ShutterCallback shutter, PictureCallback jpeg) {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                PictureStartCallback startCallback = new PictureStartCallback();
                startCallback.mDelegate = shutter;
                PictureDoneCallback doneCallback = new PictureDoneCallback();
                doneCallback.mDelegate = jpeg;
                mCamera.takePicture(startCallback, null, null, doneCallback);
            }
        }
    }


    @Nullable
    @FocusMode
    public String getFocusMode() {
        return mFocusMode;
    }


    public boolean setFocusMode(@FocusMode String mode) {
        synchronized (mCameraLock) {
            if (mCamera != null && mode != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getSupportedFocusModes().contains(mode)) {
                    parameters.setFocusMode(mode);
                    mCamera.setParameters(parameters);
                    mFocusMode = mode;
                    return true;
                }
            }

            return false;
        }
    }


    public String getFlashMode() {
        return mFlashMode;
    }


    public boolean setFlashMode(@FlashMode String mode) {
        synchronized (mCameraLock) {
            if (mCamera != null && mode != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getSupportedFlashModes().contains(mode)) {
                    parameters.setFlashMode(mode);
                    mCamera.setParameters(parameters);
                    mFlashMode = mode;
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Starts camera auto-focus and registers a callback function to run when
     * the camera is focused.  This method is only valid when preview is active
     * (between {@link #start()} or {@link #start(SurfaceHolder)} and before {@link #stop()}
     * or {@link #release()}).
     * <p/>
     * <p>Callers should check
     * {@link #getFocusMode()} to determine if
     * this method should be called. If the camera does not support auto-focus,
     * it is a no-op and {@link AutoFocusCallback#onAutoFocus(boolean)}
     * callback will be called immediately.
     * <p/>
     * <p>If the current flash mode is not
     * {@link Camera.Parameters#FLASH_MODE_OFF}, flash may be
     * fired during auto-focus, depending on the driver and camera hardware.<p>
     *
     * @param cb the callback to run
     * @see #cancelAutoFocus()
     */
    public void autoFocus(@Nullable AutoFocusCallback cb) {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                CameraAutoFocusCallback autoFocusCallback = null;
                if (cb != null) {
                    autoFocusCallback = new CameraAutoFocusCallback();
                    autoFocusCallback.mDelegate = cb;
                }
                mCamera.autoFocus(autoFocusCallback);
            }
        }
    }

    /**
     * Cancels any auto-focus function in progress.
     * Whether or not auto-focus is currently in progress,
     * this function will return the focus position to the default.
     * If the camera does not support auto-focus, this is a no-op.
     *
     * @see #autoFocus(AutoFocusCallback)
     */
    public void cancelAutoFocus() {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                mCamera.cancelAutoFocus();
            }
        }
    }

    /**
     * Sets camera auto-focus move callback.
     *
     * @param cb the callback to run
     * @return {@code true} if the operation is supported (i.e. from Jelly Bean), {@code false}
     * otherwise
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean setAutoFocusMoveCallback(@Nullable AutoFocusMoveCallback cb) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }

        synchronized (mCameraLock) {
            if (mCamera != null) {
                CameraAutoFocusMoveCallback autoFocusMoveCallback = null;
                if (cb != null) {
                    autoFocusMoveCallback = new CameraAutoFocusMoveCallback();
                    autoFocusMoveCallback.mDelegate = cb;
                }
                mCamera.setAutoFocusMoveCallback(autoFocusMoveCallback);
            }
        }

        return true;
    }

    //==============================================================================================
    // Private
    //==============================================================================================

    /**
     * Only allow creation via the builder class.
     */
    protected CameraSource() {

    }


    /**
     * Wraps the camera1 shutter callback so that the deprecated API isn't exposed.
     */
    private class PictureStartCallback implements Camera.ShutterCallback {
        private ShutterCallback mDelegate;

        @Override
        public void onShutter() {
            if (mDelegate != null) {
                mDelegate.onShutter();
            }
        }
    }

    /**
     * Wraps the final callback in the camera sequence, so that we can automatically turn the camera
     * preview back on after the picture has been taken.
     */
    private class PictureDoneCallback implements Camera.PictureCallback {
        private PictureCallback mDelegate;

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mDelegate != null) {
                mDelegate.onPictureTaken(data);
            }
            synchronized (mCameraLock) {
                if (mCamera != null) {
                    mCamera.startPreview();
                }
            }
        }
    }

    /**
     * Wraps the camera1 auto focus callback so that the deprecated API isn't exposed.
     */
    private class CameraAutoFocusCallback implements Camera.AutoFocusCallback {
        private AutoFocusCallback mDelegate;

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (mDelegate != null) {
                mDelegate.onAutoFocus(success);
            }
        }
    }

    /**
     * Wraps the camera1 auto focus move callback so that the deprecated API isn't exposed.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private class CameraAutoFocusMoveCallback implements Camera.AutoFocusMoveCallback {
        private AutoFocusMoveCallback mDelegate;

        @Override
        public void onAutoFocusMoving(boolean start, Camera camera) {
            if (mDelegate != null) {
                mDelegate.onAutoFocusMoving(start);
            }
        }
    }


    @SuppressLint("InlinedApi")
    private Camera createCamera() {

        try {
            int requestedCameraId = getIdForRequestedCamera(mFacing);
            if (requestedCameraId == -1) {
                throw new RuntimeException("Could not find requested camera.");
            }

            if (camera == null)
                camera = Camera.open(requestedCameraId);

            Size pictureSize = sizePair.pictureSize();
            mPreviewSize = sizePair.previewSize();

            Camera.Parameters parameters = camera.getParameters();

            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
            }

            parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            setRotation(camera, parameters, requestedCameraId);

            if (mFocusMode != null) {
                if (parameters.getSupportedFocusModes().contains(
                        mFocusMode)) {
                    parameters.setFocusMode(mFocusMode);
                } else {
                    Log.i(TAG, "Camera focus mode: " + mFocusMode +
                            " is not supported on this device.");
                }
            }

            // setting mFocusMode to the one set in the params
            mFocusMode = parameters.getFocusMode();


            if (mFlashMode != null) {
                if (parameters.getSupportedFlashModes().contains(
                        mFlashMode)) {
                    parameters.setFlashMode(mFlashMode);
                } else {
                    Log.i(TAG, "Camera flash mode: " + mFlashMode +
                            " is not supported on this device.");
                }
            }

           //  setting mFlashMode to the one set in the params
            mFlashMode = parameters.getFlashMode();

            autoFocus(null);


            camera.setParameters(parameters);

            // Four frame buffers are needed for working with the camera:
            //
            //   one for the frame that is currently being executed upon in doing detection
            //   one for the next pending frame to process immediately upon completing detection
            //   two for the frames that the camera uses to populate future preview images
            camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        } catch (Exception e) {

        }

        return camera;
    }


    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");
            }
        }
    };


    protected static int getIdForRequestedCamera(int facing) {
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }


    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager windowManager =
                (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (rotationMode == ROTATION_MODE_PORTRAIT) {
            degrees = 0;
        } else {
            degrees = 90;
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int angle;
        int displayAngle;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - angle); // compensate for it being mirrored
        } else {  // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }

        mRotation = angle / 90;

        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(angle);
    }



    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;


        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {

            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        mBytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mFrameProcessor.setNextFrame(data, camera);
//            camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        }
    }


}
