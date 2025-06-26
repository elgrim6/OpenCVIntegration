package mz.bancounico.uocr.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.lang.ref.WeakReference;

import mz.bancounico.uocr.R;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzParser;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzRecord;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;
import mz.bancounico.uocr.utils.TesseractResourceManager;
import mz.bancounico.uocr.utils.threads.MRZCroperTaskdManager;
import mz.bancounico.uocr.utils.threads.MRZCroperTask;
import mz.bancounico.uocr.utils.threads.UiThreadCallback;
import mz.bancounico.uocr.utils.threads.Util;

/**
 * A simple {@link Fragment} subclass.
 */

public abstract class MRZDocumentCaptureFragment extends CameraViewFragment implements UiThreadCallback {


    private int detectionsCounter;
    private boolean processed;
    private UiHandler mUiHandler;
    private MRZCroperTaskdManager mMRZCroperTaskdManager;
    private TesseractResourceManager resourceManager;

    public MRZDocumentCaptureFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initialize the handler for UI thread to handle message from worker threads
        mUiHandler = new UiHandler(Looper.getMainLooper());

        // get the thread pool manager instance
        mMRZCroperTaskdManager = MRZCroperTaskdManager.getInstance();
        // MRZCroperTaskdManager stores activity as a weak reference. No need to unregister.
        mMRZCroperTaskdManager.setUiThreadCallback(this);
        resourceManager = new TesseractResourceManager(getContext());
    }

    public abstract void onMrzFound(MrzRecord mrz);

    public abstract void onMrzNotFound();

    public void scanMRZ() {

        showScanAnimation();
        processed = false;
        detectionsCounter = 0;

        FrameProcessor frameProcessor = new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(Frame frame) {

                if (mMRZCroperTaskdManager.getTasksNumber() < MRZCroperTaskdManager.DEFAULT_THREAD_POOL_SIZE) {

                    if (frame.getData() != null) {

                        try {

                            final Frame finalFrame = frame.freeze();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        if (detectionsCounter <= MRZCroperTaskdManager.DETECTION_MAX_ATTEMPS) {

                                            Log.d("MESSAGE_CALLABLE", "THREAD ADDED");

                                            MRZCroperTask mrzCroperTask = new MRZCroperTask();
                                            mrzCroperTask.init(mMRZCroperTaskdManager, finalFrame, getCameraView(), viewFinderLayout, getThresholdFactor(detectionsCounter));
                                            mrzCroperTask.setFuture(mMRZCroperTaskdManager.addTask(mrzCroperTask));
                                            detectionsCounter++;

                                        } else {

                                            if (mMRZCroperTaskdManager.getTasksNumber() == 0 && !processed) {
                                                processed = true;
                                                hideScanAnimation();
                                                onMrzNotFound();
                                                clearFrameProcessors();
                                            }

                                        }

                                    } catch (Exception e) {
                                        Log.d("MESSAGE_COUNTER", "ERROR");
                                    }
                                }
                            });

                        } catch (Exception e) {

                        }

                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        addFrameProcessor(frameProcessor);
    }

    public int getThresholdFactor(int detectionsCounter) {

        switch (detectionsCounter) {
            case 0:
                return 37;
            case 1:
                return 15;
            case 2:
                return 9;
            case 3:
                return 13;
            case 4:
                return 25;
            case 5:
                return 11;
            case 6:
                return 15;
            case 7:
                return 7;
        }
        return 25;

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void showNotDetectedProprieties() {

        String message = "";
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Número de operações")
                .setMessage(message)
                .setPositiveButton(R.string.confirm_picture, (dialogInterface, i) -> {

                    hideScanAnimation();
                    dialogInterface.dismiss();
                    returnResult();
                })
                .setNegativeButton(R.string.retry, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    showScanAnimation();
                    scanMRZ();
                })
                .show();
    }

    public abstract void returnResult();

    private class UiHandler extends Handler {
        private WeakReference<TextView> mWeakRefDisplay;
        private MrzRecord record;

        public UiHandler(Looper looper) {
            super(looper);

        }

        // This method will run on UI thread
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case Util.MESSAGE_SUCCESS:
                    if (!processed) {
                        Log.d("MESSAGE_CALLABLE", "MESSAGE DETECTED");
                        processed = true;
                        Bundle bundle = msg.getData();
                        String messsageText = bundle.getString(Util.MESSAGE_BODY, Util.EMPTY_MESSAGE);
                        record = MrzParser.parse(messsageText);
                        hideScanAnimation();
                        onMrzFound(record);
                    }
                    break;
                default:
                    break;
            }
        }
    }




    @Override
    public void publishToUiThread(Message message) {
        // add the message from worker thread to UI thread's message queue
        if (mUiHandler != null) {
            mUiHandler.sendMessage(message);
        }
    }
}
