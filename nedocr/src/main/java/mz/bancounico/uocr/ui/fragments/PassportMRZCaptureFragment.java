package mz.bancounico.uocr.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mz.bancounico.uandroidasync.TaskListener;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzRecord;
import mz.bancounico.uocr.models.Document;
import mz.bancounico.uocr.models.IdentityCard;
import mz.bancounico.uocr.models.Passport;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PassportMRZCaptureFragment extends MRZDocumentCaptureFragment {

    private Passport document;

    public PassportMRZCaptureFragment() {
    }

    public PassportMRZCaptureFragment newInstance(boolean canScanDco) {
        PassportMRZCaptureFragment fragment = new PassportMRZCaptureFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM_SCAN_DOC,canScanDco);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            canScanDoc = getArguments().getBoolean(ARG_PARAM_SCAN_DOC);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configDocumentLayout(DocumentType.getDocumentTypeFromId(DocumentType.Passport.getId()));
        super.onViewCreated(view, savedInstanceState);
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
            scanMRZ();
        }
    }

//    @Override
//    public void onPictureTaken(byte[] jpeg) {
//        if (isFrontSide) {
//            showRotationViewTip(documentPositionGuideImageViewFront);
//            OpticalCaptureActivity.frontPicture = jpeg;
//            isFrontSide = false;
//            documentSideIndicatorTextView.setText("VERSO");
//            hideScanAnimation();
//        } else {
//            OpticalCaptureActivity.backPicture = jpeg;
//            returnResult();
//        }
//    }


    @Override
    public void onMrzFound(MrzRecord record) {

        document = new Passport();
        String name = record.givenNames + " " + record.surname;
        name = name.replaceAll("5", "S");
        name = name.replaceAll("0", "O");
        document.setName(name.replaceAll("(\\s)([a-zA-Z]{1}?)(\\s)", " $2. "));


        String birthYear = (record.dateOfBirth.year < 35 ? "20" + String.format("%02d", record.dateOfBirth.year) : "19" + String.format("%02d", record.dateOfBirth.year));
        String expirationYear = "20" + String.format("%02d", record.expirationDate.year);

        String birthDate = String.format("%02d", record.dateOfBirth.day) + "/" +
                String.format("%02d", record.dateOfBirth.month) + "/" +
                birthYear;
        document.setBirthDate(birthDate);


        String validateDate = String.format("%02d", record.expirationDate.day) + "/" +
                String.format("%02d", record.expirationDate.month) + "/";

        String expirationDate = validateDate + expirationYear;
        int issuanceYear;

        issuanceYear = Integer.parseInt(expirationYear) - 5;

        String issuanceDate = validateDate + issuanceYear;

        document.setNumber(record.documentNumber);
        document.setBirthDate(birthDate);
        document.setGender(record.sex.mrz + "");
        document.setExpiryDate(expirationDate);
        document.setIssuanceDate(issuanceDate);
        document.setCountry(record.nationality);

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
    public void onMrzNotFound() {
        playCameraShoot();
        takePictureAsync(new TaskListener() {
            @Override
            public void success(Object t) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNotDetectedProprieties();
                    }
                });
            }
            @Override
            public void error(Object o) {

            }
        });
    }

    @Override
    public void onPictureTaken(byte[] jpeg) {
        OpticalCaptureActivity.frontPicture = jpeg;
    }

    public void returnResult() {
        if(document == null){
            document = new Passport();
        }
        document.setDocuemntType(Document.DocumentType.PASSPORT);
        Intent intent = getActivity().getIntent();
        intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, document);
        returnResult(intent);
    }


}
