package mz.bancounico.uocr.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import mz.bancounico.uandroidasync.TaskListener;
import mz.bancounico.uocr.R;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzRecord;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.records.MrtdTd1;
import mz.bancounico.uocr.models.Document;
import mz.bancounico.uocr.models.IdentityCard;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class IDMRZCaptureFragment extends MRZDocumentCaptureFragment {


    private IdentityCard document;


    public IDMRZCaptureFragment() {
    }


    public IDMRZCaptureFragment newInstance(boolean canScanDco) {
        IDMRZCaptureFragment fragment = new IDMRZCaptureFragment();
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
        configDocumentLayout(DocumentType.getDocumentTypeFromId(DocumentType.ID.getId()));
        super.onViewCreated(view, savedInstanceState);
        document = new IdentityCard();


    }

    @Override
    public void captureAction() {
        if (isFrontSide) {
            documentSideIndicatorTextView.setText("FRENTE");
            takePictureAsync(new TaskListener() {
                @Override
                public void success(Object t) {
                    returnResult();
                }
                @Override
                public void error(Object o) {
                }
            });
        } else {
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
    }


    @Override
    public void onMrzFound(MrzRecord record) {
        MrtdTd1 mrtdTd1 = (MrtdTd1) record;

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

        String expiryDate = String.format("%02d", record.expirationDate.day) + "/" +
                String.format("%02d", record.expirationDate.month) + "/";
        String id;

        String expirationDate = expiryDate + expirationYear;

        int age = Integer.parseInt(expirationYear) - 5 - Integer.parseInt(birthYear);
        int issuanceYear;

        if (age >= 40)
            issuanceYear = Integer.parseInt(expirationYear) - 10;
        else
            issuanceYear = Integer.parseInt(expirationYear) - 5;

        String issuanceDate = "";

        if (mrtdTd1.optional.length() > 11) {

            if (!expirationDate.equals("01/01/2001")) {
                issuanceDate = String.format("%02d", record.expirationDate.day +  1) + "/" +
                        String.format("%02d", record.expirationDate.month) + "/" + issuanceYear;
            } else {
                expirationDate = "01/01/2049";
            }

            id = mrtdTd1.optional;
        } else {

            if (!expirationDate.equals("01/01/2001")) {
                issuanceDate = expiryDate + issuanceYear;
            } else {
                expirationDate = "01/01/2049";
            }

            String[] lines = record.toMrz().split("\n");
            id = lines[1].substring(18, 22) + lines[0].substring(5, 13) + lines[1].charAt(26);
        }

        document.setNumber(id);
        document.setBirthDate(birthDate);
        document.setGender(record.sex.mrz + "");
        document.setExpiryDate(expirationDate);
        document.setIssuanceDate(issuanceDate);
        document.setCountry(record.nationality);
        document.setIssuedIn(record.nationality);

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
            playCameraShoot();
            documentSideIndicatorTextView.setText("VERSO");
        } else {

            if(document == null){
                document = new IdentityCard();
            }
            document.setDocuemntType(Document.DocumentType.IDENTITY_CARD);

            Intent intent = getActivity().getIntent();
            intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, document);
            returnResult(intent);
        }
    }
}
