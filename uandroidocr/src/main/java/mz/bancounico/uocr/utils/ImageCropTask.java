package mz.bancounico.uocr.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.List;

import mz.bancounico.uandroidasync.TaskError;
import mz.bancounico.uandroidasync.TaskListener;

/**
 * Created by dds_unico on 6/4/18.
 */

public class ImageCropTask extends AsyncTask<Void, Void, Bitmap> {

    private Bitmap imageData;
    private TaskListener<Bitmap, TaskError> imageCropListener;


    public ImageCropTask(Bitmap imageData, TaskListener<Bitmap, TaskError> imageCropListener) {
        this.imageData = imageData;
        this.imageCropListener = imageCropListener;
    }


    @Override
    protected Bitmap doInBackground(Void... voids) {

        Bitmap bitmap = imageData;
        //  Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);


        Size imageSize = new Size(bitmap.getWidth(), bitmap.getHeight());
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        List<MatOfPoint> contours = CVProcessor.findContours(src);
        src.release();

        //
        CVProcessor.Quadrilateral quad = CVProcessor.getQuadrilateral(contours, imageSize);

        if (quad != null) {
            quad.points = CVProcessor.getUpscaledPoints(quad.points, CVProcessor.getScaleRatio(imageSize));


            ByteArrayOutputStream stream = null;

            Mat imageMat = new Mat(imageSize, CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap, imageMat);

            Mat croppedImage = CVProcessor.fourPointTransform(imageMat, quad.points);
            imageMat.release();

            //croppedImage = CVProcessor.sharpenImage(croppedImage);

            Bitmap bmp = null;

            try {
                Imgproc.cvtColor(croppedImage, imageMat, Imgproc.COLOR_BGR2RGB, 4);
                bmp = Bitmap.createBitmap(croppedImage.cols(), croppedImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(croppedImage, bmp);
            } catch (CvException e) {
                Log.d("Exception", e.getMessage());
            }

            // bmp=ImageUtil.getRoundedCornerBitmap(bmp,70);


            if (bmp != null) {
                double widthProportionPercent = (double) bmp.getHeight() / (double) bmp.getWidth();
                double heightProportionPercent = (double) bmp.getWidth() / (double) bmp.getHeight();

                if (widthProportionPercent < 0.58 || heightProportionPercent < 1.0) {
                    return null;
                }
            }


            return bmp;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            imageCropListener.success(bitmap);
        } else {
            TaskError taskError = new TaskError();
            taskError.setErrorMesage("Não foi possivel recortar a imagem. Por Favor use um fundo preto por baixo do documento");
            imageCropListener.error(taskError);
        }
    }


}
