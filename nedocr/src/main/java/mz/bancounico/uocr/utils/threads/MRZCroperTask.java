package mz.bancounico.uocr.utils.threads;

import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.otaliastudios.cameraview.frame.Frame;

import java.lang.ref.WeakReference;
import java.util.concurrent.Future;

import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzParser;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzRecord;
import mz.bancounico.uocr.utils.ImageUtils;
import mz.bancounico.uocr.utils.MRZProcessor;
import mz.bancounico.uocr.utils.Tess;

import static android.graphics.Bitmap.Config.ARGB_8888;


public class MRZCroperTask implements Runnable {

    private WeakReference<UiThreadCallback> uiThreadCallbackWeakReference;
    private WeakReference<MRZCroperTaskdManager> mrzTaskManagerWeakReference;
    private Frame frame;
    private WeakReference<View> cropLayoutReference;
    private WeakReference<View> cameraViewReference;
    private int thresholdFactor;
    private int gaussianFactor;
    private int c;
    private Future future;


    public void setFuture(Future future) {
        this.future = future;
    }

    public boolean wasInterrupted() {

        return Thread.interrupted();
    }

    @Override
    public void run() {
        try {

            if (wasInterrupted()) return;

            //mThread = Thread.currentThread();
            MrzRecord record;
            TessBaseAPI tess = Tess.getNewInstance();
            String extractedText = "";

            if (wasInterrupted()) return;

            Bitmap mBitmap = ImageUtils.decodeToBitMap(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight()).copy(ARGB_8888, true);
            frame.release();

            if (wasInterrupted()) return;

            //frame.release();
            Log.d("MESSAGE_CALLABLE", "TRYING TO DETECT");
            Bitmap mrz = ImageUtils.getMrzArea(ImageUtils.cropImageInRectView(mBitmap, cropLayoutReference.get(), cameraViewReference.get()),thresholdFactor);
            mBitmap.recycle();

            if (wasInterrupted()) return;

            if (mrz != null) {
                    tess.setImage(mrz);
                    mrz.recycle();
                    if (wasInterrupted()) return;
                    extractedText = tess.getUTF8Text();
                    tess.end();
                    extractedText = MRZProcessor.correctMrz(extractedText);
                    Log.d("MESSAGE_CALLABLE", thresholdFactor+" Found " + extractedText);
            }
            else {
                mrzTaskManagerWeakReference.get().removeTask(future);
            }

            if (wasInterrupted())
                return;

            record = MrzParser.parse(extractedText);

            if (wasInterrupted())
                return;

            if (record.validComposite && record.validDateOfBirth && record.validDocumentNumber && record.validExpirationDate) {
                mrzTaskManagerWeakReference.get().cancelAllTasks();
                sendMessage(Util.MESSAGE_SUCCESS, extractedText);
            } else {
                mrzTaskManagerWeakReference.get().removeTask(future);
            }

        } catch (Exception e) {
            mrzTaskManagerWeakReference.get().removeTask(future);
        }
    }

    public void sendMessage(int messageCode, String messageBody) {
        Message message = Util.createMessage(messageCode, messageBody);

        if (mrzTaskManagerWeakReference != null
                && mrzTaskManagerWeakReference.get() != null) {

            mrzTaskManagerWeakReference.get().sendMessageToUiThread(message);
        }
    }

    public void init(MRZCroperTaskdManager MRZCroperTaskdManager, Frame frame, View cameraView, View cropLayout, int thresholdFactor) {

        this.mrzTaskManagerWeakReference = new WeakReference<MRZCroperTaskdManager>(MRZCroperTaskdManager);
        this.cameraViewReference = new WeakReference<View>(cameraView);
        this.cropLayoutReference = new WeakReference<View>(cropLayout);
        this.frame = frame;
        this.thresholdFactor = thresholdFactor;
        this.gaussianFactor = gaussianFactor;
        this.c = c;
    }
}
