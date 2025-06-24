package mz.bancounico.uocr.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.WorkerThread;

import mz.bancounico.uocr.R;
import mz.bancounico.uocr.detectors.TextDetector;
import mz.bancounico.uocr.models.PhotoTask;
import mz.bancounico.uocr.processors.TextProcessor;

import static android.content.ContentValues.TAG;


public abstract class TextCaputeFragment2 extends CameraViewFragment {

    private Detector<TextBlock> detector;
    private TextProcessor textProcessor;
    private int detectionsCounter;
    private boolean processed;
    private final static int DETECTION_LOOP = 4;
    private Object resultData;

    public TextCaputeFragment2() {

    }

    public void initDetector(TextProcessor processor) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getActivity()).build();
        textRecognizer.setProcessor(processor);
        textProcessor = processor;
        detector = textRecognizer;
        isDetectorOperacional(detector);
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

    public void showNotDetectedProprieties(HashMap<String, String> notDetectedProperties) {

        String message = "";

        for (Map.Entry<String, String> stringStringEntry : notDetectedProperties.entrySet()) {
            Map.Entry pair = stringStringEntry;
            message += pair.getValue() + "\n";
        }

        new MaterialAlertDialogBuilder(getContext(),com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Número de operações")
                .setMessage(message)
                .setPositiveButton(R.string.confirm_picture, (dialogInterface, i) -> {

                    hideScanAnimation();
                    dialogInterface.dismiss();
                    notDetectedProperties.clear();
                    returnResult();
                })
                .setNegativeButton(R.string.retry, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    showScanAnimation();
                    scanDocument();
                })
                .show();
    }

    protected void scanDocument() {

        showScanAnimation();
        detectionsCounter = 0;
        processed = false;

        FrameProcessor frameProcessor = new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(Frame frame) {

                if (detectionsCounter <= DETECTION_LOOP && !processed) {
                    frame = frame.freeze();
                    com.google.android.gms.vision.Frame outputFrame = getVisionFrame(frame);
                    frame.release();

                    detector.receiveFrame(outputFrame);
                    if (((TextDetector) textProcessor.getDetector()).lines.size() > 0) {
                        resultData = textProcessor.getDetector().getData();
                        if (textProcessor.getDetector().hasDetect()) {
                            hideScanAnimation();
                            processed = true;
                            onDataFound(resultData);
                            clearFrameProcessors();
                            return;
                        }
                    }
                    detectionsCounter++;
                } else {

                    onDataNotFound(textProcessor.getDetector().getNotDetectedProperties());
                    hideScanAnimation();
                    clearFrameProcessors();
                }


            }
        };
        addFrameProcessor(frameProcessor);
    }

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

    public abstract void onDataFound(Object data);

    public abstract void onDataNotFound( HashMap<String,String> notDetectedProperties);
}
