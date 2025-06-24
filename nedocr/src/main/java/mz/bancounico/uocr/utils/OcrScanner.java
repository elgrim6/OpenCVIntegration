package mz.bancounico.uocr.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.microblink.MicroblinkSDK;
import com.microblink.directApi.RecognizerRunner;
import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.blinkbarcode.pdf417.Pdf417Recognizer;

import java.util.Set;

import androidx.fragment.app.Fragment;

import mz.bancounico.uocr.utils.async.TaskError;
import mz.bancounico.uocr.utils.async.TaskListener;

/**
 * Created by dds_unico on 6/4/18.
 */

public class OcrScanner {

    public static void initBarcodeLib(String licenseKey,Context context){
         /*Set the Base64 license */
        MicroblinkSDK.setLicenseKey(licenseKey,context);
        com.microblink.util.Log.setLogLevel(com.microblink.util.Log.LogLevel.LOG_VERBOSE);
   }

    public  static  void getCropedImage(Bitmap imageData, TaskListener<Bitmap,TaskError> imaeCropListener){
        ImageCropTask imageCropTask=new ImageCropTask(imageData,imaeCropListener);
        imageCropTask.execute();
    }

    public static void startIdentityCardScanner(Fragment fragment, String detectionMode, int requestCode ){
       fragment.startActivityForResult(new OcrIntents().getIdentityCardScannerIntent(fragment.getContext(),detectionMode),requestCode);
    }

    public static void startIdentityCardScanner(Activity activity, String detectionMode, int requestCode ){
        activity.startActivityForResult(new OcrIntents().getIdentityCardScannerIntent(activity.getApplicationContext(),detectionMode),requestCode);
    }


    public static void scanQrCode(Fragment fragment, int requestCode){
        fragment.startActivityForResult(new OcrIntents().getQrCodeIntent(fragment.getContext()), requestCode);
    }

    public static  void scanQrCode(Activity activity, int requestCode){
        activity.startActivityForResult(new OcrIntents().getQrCodeIntent(activity.getApplicationContext()), requestCode);
    }

    public static void detectPrepaidDigits(Activity activity, String detectionMode, int requestCode){
        activity.startActivityForResult(new OcrIntents().getDetectedPrepaidDigitsIntent(activity.getApplicationContext(),detectionMode), requestCode);
    }

    public static void detectPrepaidDigits(Fragment fragment, String detectionMode, int requestCode){
        fragment.startActivityForResult(new OcrIntents().getDetectedPrepaidDigitsIntent(fragment.getContext(),detectionMode), requestCode);
    }

}
