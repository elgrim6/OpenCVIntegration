package mz.bancounico.uocr.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;

import java.io.IOException;

import mz.bancounico.uocr.R;
import mz.bancounico.uocr.graphics.OcrGraphic;
import mz.bancounico.uocr.models.PhotoTask;
import mz.bancounico.uocr.logic.CameraSource;
import mz.bancounico.uocr.logic.CameraSourcePreview;
import mz.bancounico.uocr.logic.GraphicOverlay;

import static android.content.ContentValues.TAG;


public abstract class BaseCaptureFragment extends Fragment {

    protected CameraSource mCameraSource;
    protected CameraSourcePreview mPreview;
    protected GraphicOverlay<OcrGraphic> mGraphicOverlay;
    protected ImageButton scanButton;
    protected View scanBar;
    protected boolean isLandscape = false;
    private boolean autoFocus;
    private boolean useFlash;
    private String detectionMode;
    private int rotationMode;
    protected Handler cameraSourceHandler;
    protected byte[] frontPicture;
    protected byte[] backPicture;
    private boolean isOpenCvConnected = false;
    protected View rootView;
    protected PhotoTask photoTask;


    public BaseCaptureFragment() {
        // Required empty public constructor
    }

    public void setTouchListeners() {

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mCameraSource.focusOnTouch(motionEvent.getRawX(), motionEvent.getRawY());
                return true;
            }
        });
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startScan();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTouchListeners();
        setCameraSourceHandler();
        createCameraSource();
    }

    public void setCameraSourceHandler() {
        cameraSourceHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                mGraphicOverlay.clear();
                hideScanAnimation();
                onScanResult(msg);

            }
        };
    }

    public abstract void onScanResult(Message msg);

    public void startScan() {
        try {
            mCameraSource.focusOnTouch(mCameraSource.getPreviewSize().getWidth() / 2, mCameraSource.getPreviewSize().getHeight() / 2);
        } catch (NullPointerException e) {

        }
        mCameraSource.startProcessing();
        showScanAnimation();
    }

    public void showScanAnimation() {
        final Animation animation = AnimationUtils.loadAnimation(getActivity(), isLandscape ? R.anim.scan_animation_land : R.anim.scan_animation_portrait);
        scanBar.setVisibility(View.VISIBLE);
        scanBar.startAnimation(animation);
    }

    public void hideScanAnimation() {
        scanBar.setVisibility(View.GONE);
        scanBar.clearAnimation();
    }

    public boolean isDetectorOperacional(Detector<?> detector) {
        if (!detector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = getActivity().registerReceiver(null, lowstorageFilter) != null;
            if (hasLowStorage) {
                Toast.makeText(getActivity(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
            return false;
        }

        return true;
    }


    public void returnResult(Intent intent) {
        //mCameraSource.release();
        //OpticalCaptureActivity.setImages(frontPicture,backPicture);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    protected void startCameraSource() throws SecurityException {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public abstract void createCameraSource();

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }




    @Override
    public void onPause() {
        super.onPause();
        // Faz realease do camera source e do preview
        // O realease da camera esta no cameraSource
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // Faz realease do camera source e do preview
        // O realease da camera esta no cameraSource
        if (mPreview != null) {
            mPreview.release();
        }
    }



}