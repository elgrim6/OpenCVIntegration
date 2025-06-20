package mz.bancounico.uocr.logic;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextRecognizer;

import java.nio.ByteBuffer;
import java.util.HashMap;

import mz.bancounico.uocr.detectors.TextDetector;
import mz.bancounico.uocr.interfaces.PictureCallback;
import mz.bancounico.uocr.interfaces.ShutterCallback;
import mz.bancounico.uocr.models.Document;
import mz.bancounico.uocr.models.PhotoTask;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * Created by dds_unico on 12/6/18.
 */

public class FrameProcessingRunnable implements Runnable {
    private Detector<?> mDetector;
    private long mStartTimeMillis = SystemClock.elapsedRealtime();

    // This lock guards all of the member variables below.

    private boolean mActive = true;

    // These pending variables hold the state associated with the new frame awaiting processing.
    private long mPendingTimeMillis;
    private int mPendingFrameId = 0;
    private int detectionsCounter = 0;
    private ByteBuffer mPendingFrameData;
    private boolean isReading = false;
    private Object resultData;

    protected final int maxDetectionLoop = 5;

    private CameraSource cameraSource;

    FrameProcessingRunnable(CameraSource cameraSource, Detector<?> detector) {
        mDetector = detector;
        this.cameraSource = cameraSource;
    }


    @SuppressLint("Assert")
    void release() {
        assert (cameraSource.mProcessingThread.getState() == Thread.State.TERMINATED);
        if (mDetector != null)
            mDetector.release();
        mDetector = null;
    }

    /**
     * Marks the runnable as active/not active.  Signals any blocked threads to continue.
     */
    void setActive(boolean active) {
        synchronized (cameraSource.mLock) {
            mActive = active;
            cameraSource.mLock.notifyAll();
        }
    }

    /**
     * Sets the frame data received from the camera.  This adds the previous unused frame buffer
     * (if present) back to the camera, and keeps a pending reference to the frame data for
     * future use.
     */
    void setNextFrame(byte[] data, Camera camera) {
        synchronized (cameraSource.mLock) {
            if (mPendingFrameData != null) {
                camera.addCallbackBuffer(mPendingFrameData.array());
                mPendingFrameData = null;
            }

            if (!cameraSource.mBytesToByteBuffer.containsKey(data)) {
                Log.d(TAG,
                        "Skipping frame.  Could not find ByteBuffer associated with the image " +
                                "data from the camera.");
                return;
            }

            // Timestamp and frame ID are maintained here, which will give downstream code some
            // idea of the timing of frames received and when frames were dropped along the way.
            mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTimeMillis;
            mPendingFrameId++;
            mPendingFrameData = cameraSource.mBytesToByteBuffer.get(data);

            // Notify the processor thread if it is waiting on the next frame (see below).
            cameraSource.mLock.notifyAll();
        }
    }


    void stopAndTakePicture(HashMap<String, String> notDetectedProperties) {

        cameraSource.takePicture(new ShutterCallback() {
            @Override
            public void onShutter() {
                detectionsCounter = 0;
            }
        }, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data) {

                if (resultData instanceof Document) {
                    Document document = (Document) resultData;
                    PhotoTask photoTask = new PhotoTask(data, document, notDetectedProperties);
                    Message completeMessage = cameraSource.ocrHandler.obtainMessage(1, photoTask);
                    completeMessage.sendToTarget();
                    cameraSource.stopProcessingThread();
                } else if (resultData instanceof String) {
                    String resultString = (String) resultData;
                    PhotoTask photoTask = new PhotoTask(data, resultString, notDetectedProperties);
                    Message completeMessage = cameraSource.ocrHandler.obtainMessage(1, photoTask);
                    completeMessage.sendToTarget();
                    cameraSource.stopProcessingThread();
                } else {
                    PhotoTask photoTask = new PhotoTask(data, notDetectedProperties);
                    Message completeMessage = cameraSource.ocrHandler.obtainMessage(1, photoTask);
                    completeMessage.sendToTarget();
                    cameraSource.stopProcessingThread();
                }


            }
        });


    }

    void tryToDetectText(Frame outputFrame) {

        if (detectionsCounter < maxDetectionLoop) {
            detectionsCounter++;
            mDetector.receiveFrame(outputFrame);
            if (((TextDetector) cameraSource.processor.getDetector()).lines.size() > 0) {
                resultData = cameraSource.processor.getDetector().getData();
                if (cameraSource.processor.getDetector().hasDetect()) {
                    detectionsCounter = maxDetectionLoop;

                    if (!cameraSource.pictureTaken) {
                        cameraSource.pictureTaken = true;
                        stopAndTakePicture(null);
                    }
                }
            }

        } else {

            if (!cameraSource.pictureTaken) {
                cameraSource.pictureTaken = true;
                stopAndTakePicture(cameraSource.processor.getDetector().getNotDetectedProperties());

            }
        }
    }

    void stopAndGetBarcodes(SparseArray<Barcode> barcodes) {
        if(!cameraSource.canCaptureBarcodeImage) {
            sendBarcodeData(barcodes,null);
        }
        else {
            cameraSource.takePicture(new ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data) {
                    sendBarcodeData(barcodes, data);
                }
            });
        }
    }

    void sendBarcodeData(SparseArray<Barcode> barcodes, byte[] data){
        PhotoTask photoTask = new PhotoTask(barcodes);
        photoTask.setPhotoData(data);
        Message completeMessage = cameraSource.ocrHandler.obtainMessage(1, photoTask);
        completeMessage.sendToTarget();
        //stopProcessingThread();
    }

    void tryToDetectBarcodes(Frame outputFrame) {

        if (!cameraSource.barcodeDetected && !isReading) {
            isReading = true;
            SparseArray<Barcode> barcodes = (SparseArray<Barcode>) mDetector.detect(outputFrame);
            if (barcodes.size() > 0) {
                stopAndGetBarcodes(barcodes);
                cameraSource.barcodeDetected = true;

            }
        } else {
            isReading = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cameraSource.focusOnTouch(cameraSource.mPreviewSize.getWidth() / 2, cameraSource.mPreviewSize.getHeight() / 2);
        }
    }


    /**
     * As long as the processing thread is active, this executes detection on frames
     * continuously.  The next pending frame is either immediately available or hasn't been
     * received yet.  Once it is available, we transfer the frame info to local variables and
     * run detection on that frame.  It immediately loops back for the next frame without
     * pausing.
     * <p/>
     * If detection takes longer than the time in between new frames from the camera, this will
     * mean that this loop will run without ever waiting on a frame, avoiding any context
     * switching or frame acquisition time latency.
     * <p/>
     * If you find that this is using more CPU than you'd like, you should probably decrease the
     * FPS setting above to allow for some idle time in between frames.
     */
    @Override
    public void run() {
        Frame outputFrame;
        ByteBuffer data;

        while (true) {

            synchronized (cameraSource.mLock) {

                while (mActive && (mPendingFrameData == null)) {
                    try {
                        // Wait for the next frame to be received from the camera, since we
                        // don't have it yet.
                        cameraSource.mLock.wait();
                    } catch (Exception e) {
                        Log.d(TAG, "Frame processing loop terminated.", e);
                        return;
                    }
                }

                if (!mActive) {
                    // Exit the loop once this camera source is stopped or released.  We check
                    // this here, immediately after the wait() above, to handle the case where
                    // setActive(false) had been called, triggering the termination of this
                    // loop.
                    return;
                }

                outputFrame = new Frame.Builder()
                        .setImageData(mPendingFrameData, cameraSource.mPreviewSize.getWidth(),
                                cameraSource.mPreviewSize.getHeight(), ImageFormat.NV21)
                        .setId(mPendingFrameId)
                        .setTimestampMillis(mPendingTimeMillis)
                        .setRotation(cameraSource.mRotation)
                        .build();

                // Hold onto the frame data locally, so that we can use this for detection
                // below.  We need to clear mPendingFrameData to ensure that this buffer isn't
                // recycled back to the camera before we are done using that data.
                data = mPendingFrameData;
                mPendingFrameData = null;
            }

            // The code below needs to run outside of synchronization, because this will allow
            // the camera to add pending frame(s) while we are running detection on the current
            // frame.

            try {
                if (mDetector instanceof TextRecognizer) {
                    tryToDetectText(outputFrame);
                } else {
                    if (mDetector instanceof BarcodeDetector) {
                        tryToDetectBarcodes(outputFrame);
                    }
                }

            } catch (Throwable t) {
                Log.e(TAG, "Exception thrown from receiver.", t);
            } finally {
                if (cameraSource.mCamera != null)
                    cameraSource.mCamera.addCallbackBuffer(data.array());
            }
        }
    }


}
