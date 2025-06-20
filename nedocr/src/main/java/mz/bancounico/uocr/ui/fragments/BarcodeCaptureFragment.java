package mz.bancounico.uocr.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.RecognizerRunner;
import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkbarcode.barcode.BarcodeRecognizer;
import com.microblink.geometry.Rectangle;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.FeatureNotSupportedException;
import com.microblink.recognition.RecognitionSuccessType;
import com.microblink.view.recognition.ScanResultListener;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import mz.bancounico.uocr.R;
import mz.bancounico.uocr.models.BarcodeEntities.DocumentUBarcode;
import mz.bancounico.uocr.models.BarcodeEntities.DriverLicenseUBarcode;
import mz.bancounico.uocr.utils.ImageUtils;

import static android.content.ContentValues.TAG;
import static android.graphics.Bitmap.Config.ARGB_8888;


public abstract class BarcodeCaptureFragment extends CameraViewFragment {


    private RecognizerRunner mRecognizerRunner;
    private BarcodeRecognizer mRecognizer;
    private RecognizerBundle mRecognizerBundle;

    private String LOG_TAG = "BARCODE_LOG";

    public BarcodeCaptureFragment() {
    }

    private boolean hasTypeDetected = false;
    private final static int DETECTION_LOOP = 20;
    private int detectionsCounter;
    private boolean processed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBarcodeReader();
    }

    public abstract void onBarcodeFound(DocumentUBarcode documentUBarcode);

    public abstract void onBarcodeNotFound();

    private void initBarcodeReader() {
        // initialize your activity here
        // create BarcodeRecognizer
        mRecognizer = new BarcodeRecognizer();
//        mRecognizer.setAutoScaleDetection(true);
//        mRecognizer.setReadCode39AsExtendedData(true);
        mRecognizer.setScanPdf417(true);
//        mRecognizer.setScanCode39(true);
//        mRecognizer.setScanEan13(true);
//        mRecognizer.setReadCode39AsExtendedData(true);
//        mRecognizer.setScanCode128(true);
        mRecognizer.setScanQrCode(true);
        mRecognizer.setScanEan13(true);
        mRecognizer.setScanDataMatrix(true);


        // bundle recognizers into RecognizerBundle
        mRecognizerBundle = new RecognizerBundle(mRecognizer);

        try {
            mRecognizerRunner = RecognizerRunner.getSingletonInstance();
        } catch (FeatureNotSupportedException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Feature not supported! Reason: " + e.getReason().getDescription(), Toast.LENGTH_LONG).show();
            return;
        }

        mRecognizerRunner.initialize(getActivity().getApplicationContext(), mRecognizerBundle, new DirectApiErrorListener() {
            @Override
            public void onRecognizerError(Throwable t) {
                Toast.makeText(getActivity().getApplicationContext(), "There was an error in initialization of Recognizer: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecognizerRunner.terminate();
    }


    void scanBarcode() {

        processed = false;
        detectionsCounter = 0;

        FrameProcessor frameProcessor = frame -> {

                if (detectionsCounter <= DETECTION_LOOP && !processed) {
                    frame = frame.freeze();
                    getBarcode(frame);
                    frame.release();
                    detectionsCounter++;
                } else {
                    hideScanAnimation();
                    showNotDetectedProprieties();
                    clearFrameProcessors();
                }
        };
        getCameraView().addFrameProcessor(frameProcessor);
    }


    private void getBarcode(Frame frame) {

        Bitmap mBitmap = ImageUtils.decodeToBitMap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight()).copy(ARGB_8888, true);
//        Image image=ImageBuilder
//                .buildImageFromCamera1NV21Frame(frame.getData(),
//                        frame.getSize().getWidth(),
//                        frame.getSize().getHeight(),
//                        Orientation.ORIENTATION_PORTRAIT,
//                        new Rectangle(0.f, 0.f, 1.f, 1.f));
//        mRecognizerRunner.recognizeImage(image.clone(),  mScanResultListener);


        RecognizerRunner.State currentState = mRecognizerRunner.getCurrentState();
        if (currentState == RecognizerRunner.State.READY) {
            mRecognizerRunner.recognizeBitmap(mBitmap,
                    Orientation.ORIENTATION_PORTRAIT,
                    new Rectangle(0.f, 0.f, 1.f, 1.f),
                    mScanResultListener);
            mBitmap.recycle();
        }
    }

    private final ScanResultListener mScanResultListener = new ScanResultListener() {
        @Override
        public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
            // this method is from ScanResultListener and will be called
            // when scanning completes
            // you can obtain scanning result by calling getResult on each
            // recognizer that you bundled into RecognizerBundle.
            // for example:

            BarcodeRecognizer.Result result = mRecognizer.getResult();
            if (result.getResultState() == Recognizer.Result.State.Valid) {
                // result is valid, you can use it however you wish

                try {
                    DriverLicenseUBarcode driverLicenseUBarcode = new DriverLicenseUBarcode(result.getRawData());
                    onBarcodeFound(driverLicenseUBarcode);
                    processed = true;
                    clearFrameProcessors();
                    Log.w(TAG, "BARCODE_DATA: " + new String(result.getRawData(), StandardCharsets.ISO_8859_1));
                }catch (Exception e){

                }
               // mRecognizerRunner.terminate();

//                Toast.makeText(getActivity(), result.getStringData(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected String decode(Frame frame) {

        Bitmap bitmap = ImageUtils.decodeToBitMap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight()).copy(ARGB_8888, true);
        bitmap = ImageUtils.cropImageInRectView(bitmap, viewFinderLayout, getCameraView());

        Reader reader = new MultiFormatReader();
        String barcode = null;

        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binary = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.PDF_417);
            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.FALSE);
            decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            decodeHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);



            Result result = reader.decode(binary, decodeHints);
            // BarcodeFormat format = result.getBarcodeFormat();
            // ResultPoint[] points = result.getResultPoints();
            // byte[] bytes = result.getRawBytes();
            if(result!=null) {
                barcode = result.getText();
                Log.d(TAG, "BARCODE_DATA: " + barcode);
            }

        } catch (NotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "BARCODE_ERROR_NOT_FOUND: " + e.getMessage());
        } catch (FormatException e) {
            e.printStackTrace();
            Log.e(TAG, "BARCODE_ERROR_FORMAT: " + e.getMessage());
        } catch (ChecksumException e) {
            e.printStackTrace();
            Log.e(TAG, "BARCODE_ERROR_CHECKSUM: " + e.getMessage());
        }

        return barcode;
    }
//    private Barcode getBarcode(com.google.android.gms.vision.Frame frame) {
//        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getActivity().getApplicationContext())
//                .setBarcodeFormats(Barcode.ALL_FORMATS)
//                .build();
//
//        Barcode barcode=null;
//
//        if (isDetectorOperacional(barcodeDetector)) {
//            try {
//
//                SparseArray<Barcode> sparseArray = barcodeDetector.detect(frame);
//                if (sparseArray != null && sparseArray.size() > 0) {
//                    for (int i = 0; i < sparseArray.size(); i++) {
//                        barcode=sparseArray.get(0);
//                        Log.d(LOG_TAG, "Value: " + sparseArray.valueAt(i).rawValue + "----" + sparseArray.valueAt(i).displayValue);
//                    }
//                } else {
//                    Log.e(LOG_TAG, "SparseArray null or empty");
//                }
//            } catch (Exception ex) {
//                //Log.e(LOG_TAG, ex.getMessage());
//            } finally {
//
//            }
//
//        } else {
//            Log.e(LOG_TAG, "Vision dependecies not downloaded");
//        }
//
//        return barcode;
//    }


    public boolean isDetectorOperacional(Detector<?> detector) {
        if (!detector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = getActivity().registerReceiver(null, lowstorageFilter) != null;
            if (hasLowStorage) {
                Toast.makeText(getActivity(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
            return false;
        }

        return true;
    }

    private com.google.android.gms.vision.Frame getVisionFrame(Frame frame) {

        return new com.google.android.gms.vision.Frame.Builder()
                .setImageData(ByteBuffer.wrap(frame.getData()), frame.getSize().getWidth(),
                        frame.getSize().getHeight(), ImageFormat.NV21)
                .setId(0)
                .setTimestampMillis(frame.getTime())
                .setRotation(0)
                .build();
    }


    public void showNotDetectedProprieties() {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.not_detected_data);
        String message = "";

        builder.setMessage(message);
        builder.setPositiveButton(R.string.confirm_picture, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //  onBarcodeFound();
            }
        });

        builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                scanBarcode();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

}
