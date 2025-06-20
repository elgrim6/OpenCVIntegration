package mz.bancounico.uocr.utils;

import android.content.Context;
import android.content.Intent;

import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;

/**
 * Created by dds_unico on 6/5/18.
 */

public class OcrIntents {

    protected Intent getIdentityCardScannerIntent(Context context, String detectionMode){
        Intent intent = new Intent(context, OpticalCaptureActivity.class);

        intent.putExtra(OpticalCaptureActivity.AUTO_FOCUS, true);
        intent.putExtra(OpticalCaptureActivity.USE_FLASH, false);
        //intent.putExtra(OpticalCaptureActivity.ROTATION_MODE, CameraSource.ROTATION_MODE_LANDSCAPE);
        intent.putExtra(OpticalCaptureActivity.DETECTION_MODE, detectionMode);

        return  intent;
    }

    protected Intent getQrCodeIntent(Context context){
        Intent intent = new Intent(context, OpticalCaptureActivity.class);
        intent.putExtra(OpticalCaptureActivity.DETECTION_MODE, OpticalCaptureActivity.RESULT_BAR_CODE);

        return intent;
    }


    protected Intent getDetectedPrepaidDigitsIntent(Context context, String detectionMode){
        Intent intent = new Intent(context, OpticalCaptureActivity.class);
        intent.putExtra(OpticalCaptureActivity.AUTO_FOCUS, true);
        intent.putExtra(OpticalCaptureActivity.USE_FLASH, false);
        //intent.putExtra(OpticalCaptureActivity.ROTATION_MODE, CameraSource.ROTATION_MODE_LANDSCAPE);
        intent.putExtra(OpticalCaptureActivity.DETECTION_MODE, OpticalCaptureActivity.RESULT_REGEX);
        intent.putExtra(OpticalCaptureActivity.REGEX_PROPERTY_NAME, "Numero do Cartão");
        intent.putExtra(OpticalCaptureActivity.REGEX_EXPRESSION, "^(457209)(\\d{10})$");
        return intent;
    }
}
