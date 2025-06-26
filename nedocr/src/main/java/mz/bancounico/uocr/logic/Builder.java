package mz.bancounico.uocr.logic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;

import com.google.android.gms.vision.Detector;

import java.util.List;

import mz.bancounico.uocr.processors.Processor;

import static mz.bancounico.uocr.logic.CameraSource.getIdForRequestedCamera;

/**
 * Created by dds_unico on 12/6/18.
 */


public  class Builder {
    private final Detector<?> mDetector;
    private static CameraSource mCameraSource = new CameraSource();

    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;
    public static final int DEFAULT_MAX_WIDTH = 1280;

    public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;




    private SizePair getBestPair(Camera camera, int maxWidth){

        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        SizePair bestPair = null;

        for (Camera.Size previewSize : supportedPreviewSizes) {

            if(previewSize.width <= maxWidth){

                for (Camera.Size pictureSize : supportedPictureSizes) {

                    if(pictureSize.width == previewSize.width && pictureSize.height == previewSize.height && pictureSize.width != pictureSize.height){

                        bestPair = new SizePair(previewSize,pictureSize);
                        return bestPair;
                    }
                }
            }
        }

        return bestPair;
    }


        /*
        private  SizePair generateValidPreviewSize(Camera camera, int desiredWidth,
                                                         int desiredHeight) {
            Camera.Parameters parameters = camera.getParameters();
            double screenAspectRatio = desiredWidth / (double) desiredHeight;
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            SizePair bestPair = null;
            double currentMinDistortion = MAX_ASPECT_DISTORTION;
            for (Camera.Size previewSize : supportedPreviewSizes) {
                float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;
                for (Camera.Size pictureSize : supportedPictureSizes) {
                    float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                    if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                        SizePair sizePair = new SizePair(previewSize, pictureSize);

                        boolean isCandidatePortrait = previewSize.width < previewSize.height;
                        int maybeFlippedWidth = isCandidatePortrait ? previewSize.width : previewSize.height;
                        int maybeFlippedHeight = isCandidatePortrait ? previewSize.height : previewSize.width;
                        double aspectRatio = maybeFlippedWidth / (double) maybeFlippedHeight;
                        double distortion = Math.abs(aspectRatio - screenAspectRatio);
                        if (distortion < currentMinDistortion) {
                            currentMinDistortion = distortion;
                            bestPair = sizePair;
                            if(bestPair.previewSize().getWidth() == desiredWidth)
                                return  bestPair;
                        }
                        break;
                    }
                }
            }

            return bestPair;
        }*/

    /**
     * Creates a camera source builder with the supplied context and detector.  Camera preview
     * images will be streamed to the associated detector upon starting the camera source.
     */
    public Builder(Context context, Detector<?> detector, Handler ocrHandler, int maxWidth) {
        if (context == null) {
            throw new IllegalArgumentException("No context supplied.");
        }
        if (detector == null) {
            throw new IllegalArgumentException("No detector supplied.");
        }

        mDetector = detector;
        mCameraSource.mContext = context;
        mCameraSource.ocrHandler = ocrHandler;
        mCameraSource.mFacing = CAMERA_FACING_BACK;

        // TODO: Change camera to field param
        int requestedCameraId = getIdForRequestedCamera(mCameraSource.mFacing);
        if (requestedCameraId == -1) {
            throw new RuntimeException("Could not find requested camera.");
        }

        int maxW = maxWidth != 0? maxWidth:DEFAULT_MAX_WIDTH;

        mCameraSource.camera = Camera.open(requestedCameraId);
        mCameraSource.sizePair= getBestPair(mCameraSource.camera,maxW);
        mCameraSource.mPreviewSize = mCameraSource.sizePair.previewSize();

    }


    public Builder setFocusMode(@CameraSource.FocusMode String mode) {
        mCameraSource.mFocusMode = mode;
        return this;
    }

    public Builder setCanCaptureBarcodeImage(boolean canCaptureBarcodeImage) {
        mCameraSource.canCaptureBarcodeImage = canCaptureBarcodeImage;
        return this;
    }

    public Builder setProcessor(Processor processor) {
        mCameraSource.processor = processor;
        return this;
    }

    public Builder setFlashMode(@CameraSource.FlashMode String mode) {
        mCameraSource.mFlashMode = mode;
        return this;
    }

    public Builder setRotationMode(int rotationMode) {
        mCameraSource.rotationMode = rotationMode;
        return this;
    }



    public Builder setFacing(int facing) {
        if ((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        mCameraSource.mFacing = facing;
        return this;
    }

    /**
     * Creates an instance of the camera source.
     */
    public CameraSource build() {
        mCameraSource.mFrameProcessor = new FrameProcessingRunnable(mCameraSource,mDetector);
        return mCameraSource;
    }
}