package mz.bancounico.uocr.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import mz.bancounico.uandroidasync.TaskListener;
import mz.bancounico.uocr.detectors.DireTextDetector;
import mz.bancounico.uocr.models.Dire;
import mz.bancounico.uocr.models.Document;
import mz.bancounico.uocr.models.IdentityCard;
import mz.bancounico.uocr.processors.TextProcessor;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class DireCaptureFragment2 extends TextCaputeFragment2 {

    private boolean isFrontSide = true;
    private Dire document;
    private DireTextDetector direTextDetector;

    public DireCaptureFragment2() {
    }

    public DireCaptureFragment2 newInstance(boolean canScanDco) {
        DireCaptureFragment2 fragment = new DireCaptureFragment2();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM_SCAN_DOC,canScanDco);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        direTextDetector = new DireTextDetector(getContext(),DireTextDetector.ReadMode.Front);
        TextProcessor textProcessor = new TextProcessor(direTextDetector);
        initDetector(textProcessor);

        if (getArguments() != null) {
            canScanDoc = getArguments().getBoolean(ARG_PARAM_SCAN_DOC);
        }
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configDocumentLayout(DocumentType.getDocumentTypeFromId(DocumentType.Dire.getId()));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDataFound(Object data) {
        document = (Dire) data;

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
    public void captureAction() {
        if (isFrontSide) {
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
        } else {
            documentSideIndicatorTextView.setText("VERSO");
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
    }

    @Override
    public void onPictureTaken(byte[] jpeg) {

        if (isFrontSide) {
            OpticalCaptureActivity.frontPicture = jpeg;

        } else {
            OpticalCaptureActivity.backPicture = jpeg;
        }
    }

    @Override
    public void returnResult() {

        if (isFrontSide) {
            showRotationViewTip();
            isFrontSide = false;
            documentSideIndicatorTextView.setText("VERSO");
            direTextDetector.setParams(DireTextDetector.ReadMode.BACK);
        } else {
            Intent intent = getActivity().getIntent();
            if(document == null){
                document = new Dire();
            }

            document.setDocuemntType(Document.DocumentType.DIRE);
            intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, document);
            returnResult(intent);
        }

    }

}
