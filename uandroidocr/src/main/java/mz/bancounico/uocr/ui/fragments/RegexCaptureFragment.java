package mz.bancounico.uocr.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import mz.bancounico.uocr.R;
import mz.bancounico.uocr.detectors.RegexTextDetector;
import mz.bancounico.uocr.graphics.OcrGraphic;
import mz.bancounico.uocr.models.PhotoTask;
import mz.bancounico.uocr.processors.TextProcessor;
import mz.bancounico.uocr.logic.CameraSourcePreview;
import mz.bancounico.uocr.logic.GraphicOverlay;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;


public class RegexCaptureFragment extends TextCaputeFragment {


    private String regexExpression;
    private String propertyName;


    public RegexCaptureFragment() {
        isLandscape = true;
    }

    @Override
    public void retunTextData() {

    }
    public void setRegexExpression(String regexExpression) {
        this.regexExpression = regexExpression;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public void onScanResult(Message msg) {

        photoTask = (PhotoTask) msg.obj;
        mCameraSource.setPictureTaken(false);

        if(photoTask.getNotDetectedProperties() != null){
            showNotDetectedProprieties(photoTask);
        }
        else{
            Intent intent = getActivity().getIntent();
            intent.putExtra(OpticalCaptureActivity.RESULT_REGEX, (String) photoTask.getResult());
            returnResult(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_regex_capture, container, false);
        mPreview = (CameraSourcePreview) rootView.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) rootView.findViewById(R.id.graphicOverlay);
        scanButton = (ImageButton) rootView.findViewById(R.id.scanActionView);
        scanBar = rootView.findViewById(R.id.scanBar);

        return rootView;
    }

    @Override
    public void createCameraSource() {
        RegexTextDetector regexTextDetector = new RegexTextDetector(getActivity(),regexExpression,propertyName);
        TextProcessor textProcessor = new TextProcessor(mGraphicOverlay, regexTextDetector);
        mCameraSource = createTextCameraSource(textProcessor);
    }




}
