package mz.bancounico.uocr.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;


public class OtherTwoSidesCaptureFragment extends CameraViewFragment {


    private boolean isFrontSide = true;

    public OtherTwoSidesCaptureFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configDocumentLayout(DocumentType.getDocumentTypeFromId(DocumentType.Others_TwoSides.getId()));
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

        if (isFrontSide) {
            documentSideIndicatorTextView.setText("FRENTE");
            showRotationViewTip();
            OpticalCaptureActivity.frontPicture = jpeg;
            playCameraShoot();
            isFrontSide = false;
        }
        else
        {
            documentSideIndicatorTextView.setText("VERSO");
            OpticalCaptureActivity.backPicture = jpeg;
            playCameraShoot();
            returnResult();
        }

    }


}
