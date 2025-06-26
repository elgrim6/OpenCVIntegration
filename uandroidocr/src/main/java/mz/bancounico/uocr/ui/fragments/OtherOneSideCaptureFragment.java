package mz.bancounico.uocr.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mz.bancounico.uocr.detectors.OtherTextDetector;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;


public class OtherOneSideCaptureFragment extends CameraViewFragment {

    private OtherTextDetector otherTextDetector;

    public OtherOneSideCaptureFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configDocumentLayout(DocumentType.getDocumentTypeFromId(DocumentType.Others.getId()));
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void captureAction() {
        takePicture();
    }

    @Override
    public void returnResult() {
        Intent intent = getActivity().getIntent();
        returnResult(intent);
    }

    @Override
    public void onPictureTaken(byte[] jpeg) {
        playCameraShoot();
        OpticalCaptureActivity.frontPicture = jpeg;
        returnResult();
    }





}
