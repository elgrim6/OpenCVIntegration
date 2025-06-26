package mz.bancounico.uocr.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import mz.bancounico.uandroidasync.TaskListener;
import mz.bancounico.uocr.detectors.DriverLicenseTextDetector;
import mz.bancounico.uocr.models.Document;
import mz.bancounico.uocr.models.DriverLicense;
import mz.bancounico.uocr.models.IdentityCard;
import mz.bancounico.uocr.processors.TextProcessor;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverLicenseCaptureFragment2 extends TextCaputeFragment2 {

    private boolean isFrontSide = true;
    private DriverLicense document;

    public DriverLicenseCaptureFragment2() {
    }


    public DriverLicenseCaptureFragment2 newInstance(boolean canScanDco) {
        DriverLicenseCaptureFragment2 fragment = new DriverLicenseCaptureFragment2();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM_SCAN_DOC,canScanDco);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DriverLicenseTextDetector driverLicenseTextDetector = new DriverLicenseTextDetector(getContext());
        TextProcessor textProcessor = new TextProcessor(driverLicenseTextDetector);
        initDetector(textProcessor);

        if (getArguments() != null) {
            canScanDoc = getArguments().getBoolean(ARG_PARAM_SCAN_DOC);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configDocumentLayout(DocumentType.getDocumentTypeFromId(DocumentType.Driving_Licence.getId()));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDataFound(Object data) {
        document = (DriverLicense) data;

        takePictureAsync(new TaskListener() {
            @Override
            public void success(Object t) {
                returnResult();
            }

            @Override
            public void error(Object o) {

            }
        });
    }

    @Override
    public void onDataNotFound(HashMap<String, String> notDetectedProperties) {

        takePictureAsync(new TaskListener() {
            @Override
            public void success(Object t) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNotDetectedProprieties(notDetectedProperties);
                    }
                });
            }

            @Override
            public void error(Object o) {
            }
        });
    }


    @Override
    public void captureAction() {
        documentSideIndicatorTextView.setText("FRENTE");
        if(!canScanDoc){
            takePictureAsync(new TaskListener() {
                @Override
                public void success(Object t) {
                    returnResult();
                }

                @Override
                public void error(Object o) {

                }
            });
        }else{
            scanDocument();
        }

    }

    @Override
    public void onPictureTaken(byte[] jpeg) {
        OpticalCaptureActivity.frontPicture = jpeg;
    }

    @Override
    public void returnResult() {
        Intent intent = getActivity().getIntent();

        if(document == null){
            document = new DriverLicense();
        }

        document.setDocuemntType(Document.DocumentType.DRIVER_LICENSE);
        intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, document);
        returnResult(intent);

    }

}
