package mz.bancounico.uocr.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;

import com.google.android.gms.vision.text.TextRecognizer;

import java.util.Iterator;
import java.util.Map;

import mz.bancounico.uocr.R;
import mz.bancounico.uocr.logic.Builder;
import mz.bancounico.uocr.models.PhotoTask;
import mz.bancounico.uocr.processors.TextProcessor;
import mz.bancounico.uocr.logic.CameraSource;


public abstract class TextCaputeFragment extends BaseCaptureFragment {

    private int rotationMode;

    public TextCaputeFragment() {

    }

    public void setRotationMode(int rotationMode){
        this.rotationMode = rotationMode;
    }

    public CameraSource createTextCameraSource(TextProcessor processor) {
        return createTextCameraSource(processor, 0);
    }

    public CameraSource createTextCameraSource(TextProcessor processor, int maxPictureWidth) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getActivity()).build();
        isDetectorOperacional(textRecognizer);
        textRecognizer.setProcessor(processor);

        return new Builder(getActivity(), textRecognizer, cameraSourceHandler,maxPictureWidth)
                .setFacing(Builder.CAMERA_FACING_BACK)
                .setRotationMode(rotationMode)
                .setFlashMode(null)
                .setProcessor(processor)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO )
                .build();


    }



    public abstract void retunTextData();


    public void showNotDetectedProprieties(PhotoTask photoTask) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.not_detected_data);
        String message = "";


        Iterator it = photoTask.getNotDetectedProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            message += pair.getValue() + "\n";
        }

        builder.setMessage(message);
        builder.setPositiveButton(R.string.confirm_picture, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //returnOcrResult(photoTask);
                photoTask.getNotDetectedProperties().clear();
                retunTextData();
            }
        });

        builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startScan();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }


}
