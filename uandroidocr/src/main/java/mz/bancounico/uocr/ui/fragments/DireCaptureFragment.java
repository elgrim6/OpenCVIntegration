package mz.bancounico.uocr.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import mz.bancounico.uocr.R;
import mz.bancounico.uocr.detectors.DireTextDetector;
import mz.bancounico.uocr.graphics.OcrGraphic;
import mz.bancounico.uocr.models.Dire;
import mz.bancounico.uocr.models.PhotoTask;
import mz.bancounico.uocr.processors.TextProcessor;
import mz.bancounico.uocr.logic.CameraSource;
import mz.bancounico.uocr.logic.CameraSourcePreview;
import mz.bancounico.uocr.logic.GraphicOverlay;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;


public class DireCaptureFragment extends TextCaputeFragment {


    private DireTextDetector direTextDetector;
    private TextView sideIndicatorTextView;
    private boolean isFrontSide;

    public DireCaptureFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLandscape = true;
        isFrontSide = true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRotationMode(CameraSource.ROTATION_MODE_LANDSCAPE);
        rootView = inflater.inflate(R.layout.fragment_document_landscape, container, false);
        mPreview = (CameraSourcePreview) rootView.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) rootView.findViewById(R.id.graphicOverlay);
        scanButton = (ImageButton) rootView.findViewById(R.id.scanActionView);
        scanBar = rootView.findViewById(R.id.scanBar);
        sideIndicatorTextView = (TextView) rootView.findViewById(R.id.sideIndicatorTextView);


        return rootView;
    }

    @Override
    public void createCameraSource() {
        mCameraSource = createIdentityCardCameraSource(getActivity(), DireTextDetector.ReadMode.Front);
    }

    public CameraSource createIdentityCardCameraSource(Context context, DireTextDetector.ReadMode readMode) {
        TextProcessor textProcessor = getDireProcessor(context, readMode);
        return createTextCameraSource(textProcessor,3000);
    }

    public TextProcessor getDireProcessor(Context context, DireTextDetector.ReadMode readMode) {
        direTextDetector = new DireTextDetector(context, readMode);
        TextProcessor textProcessor = new TextProcessor(mGraphicOverlay, direTextDetector);
        return textProcessor;
    }


    public void onScanResult(Message msg) {

        photoTask = (PhotoTask) msg.obj;
        mCameraSource.setPictureTaken(false);

        if (photoTask.getNotDetectedProperties() != null) {
            showNotDetectedProprieties(photoTask);
        } else {
            Toast.makeText(getActivity(), R.string.success_capture_message, Toast.LENGTH_SHORT).show();
            retunTextData();
        }
    }


    @Override
    public void retunTextData() {

        if (isFrontSide) {
            isFrontSide = false;
            frontPicture = photoTask.getPhotoData();
            direTextDetector.setParams(DireTextDetector.ReadMode.BACK);
            sideIndicatorTextView.setText("VERSO");
        } else {

            backPicture = photoTask.getPhotoData();
            Intent intent = getActivity().getIntent();
            intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, (Dire) photoTask.getResult());
            returnResult(intent);
        }

    }


}
