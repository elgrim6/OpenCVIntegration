package mz.bancounico.uocr.ui.fragments;

import android.content.Intent;

import mz.bancounico.uocr.models.BarcodeEntities.DocumentUBarcode;
import mz.bancounico.uocr.models.BarcodeEntities.DriverLicenseUBarcode;
import mz.bancounico.uocr.models.DriverLicense;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;


public class DriverLicenseCaptureFragment extends  BarcodeCaptureFragment{

    private boolean isFrontSide = true;
    private DriverLicense document;


    public DriverLicenseCaptureFragment() {
        // Required empty public constructor
    }

    @Override
    public void captureAction() {
        int width= rootView.getWidth();
        if (isFrontSide) {
            documentSideIndicatorTextView.setText("FRENTE");
            hideScanAnimation();
            playCameraShoot();
            takePicture();
        } else {
            showScanAnimation();
            scanBarcode();
        }
    }

    @Override
    public void onPictureTaken(byte[] jpeg) {
        hideScanAnimation();
        if (isFrontSide) {
            showRotationViewTip();
            OpticalCaptureActivity.frontPicture = jpeg;
            isFrontSide = false;
            documentSideIndicatorTextView.setText("VERSO");
        } else {
            OpticalCaptureActivity.backPicture = jpeg;
            returnResult();
        }
    }

    public void returnResult() {
        Intent intent = getActivity().getIntent();
        intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, document);
        returnResult(intent);
    }

    @Override
    public void onBarcodeFound(DocumentUBarcode documentUBarcode) {

        DriverLicenseUBarcode driverLicenseUBarcode=(DriverLicenseUBarcode)documentUBarcode;
        document=new DriverLicense();
        document.setName(driverLicenseUBarcode.getName());
        document.setBirthDate(driverLicenseUBarcode.getBirthDate());
        document.setNumber(driverLicenseUBarcode.getDocumentNumber());
        document.setBirthDate(driverLicenseUBarcode.getBirthDate());
        document.setGender(driverLicenseUBarcode.getGender());
        document.setExpiryDate(driverLicenseUBarcode.getExpiryDate());
        document.setIssuanceDate(driverLicenseUBarcode.getIssuanceDate());



        playCameraShoot();
        takePicture();
    }

    @Override
    public void onBarcodeNotFound() {
        playCameraShoot();
        takePicture();
    }


}
