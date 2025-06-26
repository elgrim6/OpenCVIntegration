package mz.bancounico.uocr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Preview;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ImageUtils {

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MiB = 1024 * 1024;
    private static final long KiB = 1024;

    final int MAX_WIDTH = 1024;
    final int MAX_HEIGHT = 768;

    private Context context = null;
    private static volatile ImageUtils singleton = null;
    private int FADE_DURATION = 1000;


    private ImageUtils(Context context) {
        this.context = context;
    }

    public static ImageUtils with(Context context) {
        if (singleton == null) {
            synchronized (ImageUtils.class) {
                if (singleton == null) {
                    singleton = new ImageUtils(context);
                }
            }
        }
        return singleton;
    }


    int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));


    //Obter byteArray a partir dum Bitmap
    public static byte[] getCompressedByteArrayFromBitmap(Bitmap bmp) {
        return getCompressedByteArrayFromBitmap(bmp, 100, Bitmap.CompressFormat.JPEG);
    }

    public static byte[] getCompressedByteArrayFromBitmap(Bitmap bmp, int quality, Bitmap.CompressFormat compressFormat) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(compressFormat, quality, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    //Obter String na base64 a partir dum bitmap
    public static String getStringBase64ForImage(Bitmap bitmap) {
        return getStringBase64ForImage(bitmap, Bitmap.CompressFormat.JPEG);
    }

    //Obter String na base64 a partir dum bitmap
    public static String getStringBase64ForImage(Bitmap bitmap, Bitmap.CompressFormat compressFormat) {
        byte[] bytes = getCompressedByteArrayFromBitmap(bitmap, 100, compressFormat);
        String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        int size = base64.getBytes().length / 1024;
        return base64;
    }


//    //Obter String na base64 a partir dum File
//    public String getStringBase64FromFile(String filePath){
//
//        return ImageZipper.getBase64forImage(getCompressedImageFile(filePath));
//    }

    //Obter Bitmap a partir dunma String na base64
    public static Bitmap getBitmapFromBase64String(String base64, ImageView mImageView) {
        byte[] decodedBytes = Base64.decode(base64, 0);

        // Get the dimensions of the View
        int targetW = 720;
        int targetH = 480;

        if (mImageView != null) {
            targetW = mImageView.getWidth() > 0 ? mImageView.getWidth() : targetW;
            targetH = mImageView.getHeight() > 0 ? mImageView.getHeight() : targetH;
        }
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, targetW, targetH);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    //Obter Bitmap a partir dunma String na base64
    public Bitmap getBitmapFromBase64String(String base64) {
        return getBitmapFromBase64String(base64, null);
    }


    //Obter ImageFile em Tamanho Reduzido
//    public File getCompressedImageFile(String filePath){
//        File file=null;
//
//        Log.d("FILE SIZE", "before: " +getFileSize(new File(filePath)));
//        try {
//            file=new ImageZipper(context).compressToFile(new File(filePath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Log.d("FILE SIZE", "after: "+ getFileSize(file));
//        return  file;
//    }
//
//    public void setFiitedPhoto( String path,ImageView mImageView) {
//        mImageView.setImageBitmap(getRotatedBitmap(getDecodedBitmap(path,mImageView),path));
//    }

    public Bitmap getDecodedBitmap(String path, ImageView mImageView) {

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap decodeBitmap = BitmapFactory.decodeFile(path, bmOptions);

        return decodeBitmap;
    }


    private File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    public static String encodeToStringBase64(Bitmap bitmap) {
        byte[] b1 = getCompressedByteArrayFromBitmap(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return Base64.encodeToString(b1, Base64.DEFAULT);
        }
        return null;
    }


    public Bitmap getBitmap(File file) throws IOException {
        //new ImageZipper(context).compressToBitmap(file)
        String filePath = file.getPath();
        return BitmapFactory.decodeFile(filePath);

    }

    public void setPhoto(String base64String, ImageView imageView) {
        // Loads given image

        imageView.setImageBitmap(getBitmapFromBase64String(base64String, imageView));
    }


    //Obter Tamanho do File
    public String getFileSize(File file) {

        if (!file.isFile()) {
            throw new IllegalArgumentException("Expected a file");
        }
        final double length = file.length();

        if (length > MiB) {
            return format.format(length / MiB) + " MB";
        }
        if (length > KiB) {
            return format.format(length / KiB) + " KB";
        }
        return format.format(length) + " B";
    }

    private void setImageDrawableAnimation(Bitmap firstBitmap, Bitmap secondBitmap, int fadeDuration, ImageView imageView) {
        // create the transition layers
        Drawable[] layers = new Drawable[2];
        layers[0] = new BitmapDrawable(context.getResources(), firstBitmap);
        layers[1] = new BitmapDrawable(context.getResources(), secondBitmap);

        TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(fadeDuration == -1 ? FADE_DURATION : fadeDuration);
    }

    public void setImageDrawableAnimation(Bitmap firstBitmap, Bitmap secondBitmap, ImageView imageView) {
        setImageDrawableAnimation(firstBitmap, secondBitmap, -1, imageView);
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, Boolean enableTransparency) {
        if (enableTransparency) return getTransparentRoundedCornerBitmap(bitmap);
        else return getPaintedRoundedCornerBitmap(bitmap);
    }

    private static Bitmap getPaintedRoundedCornerBitmap(Bitmap bitmap) {
        Paint paintForRound = new Paint();
        paintForRound.setAntiAlias(true);

        int radius = (bitmap.getWidth() * 3) / 100;

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        canvas.drawColor(Color.WHITE);

        paintForRound.setARGB(255, 0, 0, 0);
        canvas.drawRoundRect(rectF, radius, radius, paintForRound);

        paintForRound.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        canvas.drawBitmap(bitmap, rect, rect, paintForRound);

        return output;
    }

    private static Bitmap getTransparentRoundedCornerBitmap(Bitmap bitmap) {
        Paint paintForRound = new Paint();
        paintForRound.setAntiAlias(true);
        paintForRound.setColor(0xff424242);
        paintForRound.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        int radius = (bitmap.getWidth() * 2) / 100;

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        canvas.drawARGB(0, 0, 0, 0);
        paintForRound.setXfermode(null);

        canvas.drawRoundRect(rectF, radius, radius, paintForRound);

        paintForRound.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paintForRound);

        return output;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static Bitmap cropPortionImage(Bitmap toBeCropped, double yPosition, double percentege) {

        return Bitmap.createBitmap(toBeCropped, 0,
                (int) (toBeCropped.getHeight() * yPosition),
                toBeCropped.getWidth(),
                (int) (toBeCropped.getHeight() * percentege));
    }

    public static Bitmap combineImagesTopBottom(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;
        int width = s.getWidth();
        int height = c.getHeight() + s.getHeight();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, c.getHeight(), null);

        return cs;
    }

    public static List<Mat> getTemplateList(Bitmap source) {
        Mat src = new Mat(source.getHeight(), source.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(source, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 35, 0);

        List<MatOfPoint> cont = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(src.clone(), cont, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        MatOfPoint2f approxCurve = new MatOfPoint2f();

        ArrayList<org.opencv.core.Rect> rects = new ArrayList<>();
        List<Mat> chars = new ArrayList<>();

        for (int i = 0; i < cont.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cont.get(i).toArray());
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            MatOfPoint pointsc = new MatOfPoint(approxCurve.toArray());
            org.opencv.core.Rect rect = Imgproc.boundingRect(pointsc);

            rect.width += 2;
            rect.height += 2;
            rect.x -= 1;
            rect.y -= 1;

            if (rect.size().height >= 30 && rect.size().width >= 11) {
                //Imgproc.rectangle(src, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(255), 2);
                rects.add(rect);
            }
        }

        Collections.sort(rects, xComparator);

        Mat chart = new Mat();
        for (org.opencv.core.Rect rect : rects) {
            chart = new Mat(src, rect);
            Imgproc.resize(chart, chart, new Size(15, 30), 0, 0, Imgproc.INTER_CUBIC);
            chars.add(chart);
        }

        src = chars.get(8);
        Bitmap result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, result);

        return chars;
    }


    public static Bitmap getMrzArea(Bitmap source, int thresholdFactor) {

        if(Thread.interrupted()) return null;

        Mat src = new Mat(source.getHeight(), source.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(source, src);

        int maxWidth = 800;
        Imgproc.resize(src, src, new Size(maxWidth, maxWidth / src.size().width * src.size().height), 0, 0, Imgproc.INTER_CUBIC);
        src = new Mat(src, new org.opencv.core.Rect(28, (src.height() / 2) - 30, src.width() - 28, (src.height() / 2) - 35));

        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Mat mask = src.clone();

        if(Thread.interrupted()) return null;

        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_TOPHAT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(23, 23)));
        Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(5, 5)));
        Imgproc.threshold(mask, mask, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        Imgproc.dilate(mask,mask,Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(21, 21)));


        if(Thread.interrupted()) return null;


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();
        mask.release();

        if(Thread.interrupted()) return null;


        RotatedRect mrzRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));
        RotatedRect rotatedRect = null;
        MatOfPoint2f mat = null;
        MatOfPoint2f pointsMat = new MatOfPoint2f();


        for (int i = 0; i < contours.size(); i++) {
            mat = new MatOfPoint2f(contours.get(i).toArray());
            rotatedRect = Imgproc.minAreaRect(mat);
            if(i == 0)
                pointsMat = mat;
            if (rotatedRect.size.width > mrzRect.size.width && rotatedRect.boundingRect().y > 5) {
                mrzRect = rotatedRect;
                pointsMat = mat;
            }

        }

        if(Thread.interrupted()) return null;


        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double approxDistance = Imgproc.arcLength(pointsMat, false) * 0.01;
        Imgproc.approxPolyDP(pointsMat, approxCurve, approxDistance, true);
        MatOfPoint pointsM = new MatOfPoint(approxCurve.toArray());
        mat.release();
        pointsMat.release();
        approxCurve.release();

        if(Thread.interrupted())
            return null;

        Point[] points = CVProcessor.sortPoints(pointsM.toArray());
        src = CVProcessor.fourPointTransform(src, points);
        pointsM.release();


        if(Thread.interrupted())
            return null;

        Imgproc.morphologyEx(src, src, Imgproc.MORPH_TOPHAT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT
                , new Size(155, 155
                )));

        if(Thread.interrupted())
            return null;


        Imgproc.resize(src, src, new Size(1400, 1400 / src.size().width * src.size().height), 0, 0, Imgproc.INTER_CUBIC);
        Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 115, thresholdFactor);
        Core.bitwise_not(src, src);

        if(Thread.interrupted())
            return null;


        //src = mask;
        Bitmap result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, result);
        src.release();

        return result;

    }

    private static Comparator<Rect> xComparator = new Comparator<org.opencv.core.Rect>() {

        @Override
        public int compare(org.opencv.core.Rect rect1, org.opencv.core.Rect rect2) {
            if (rect1.x < rect2.x)
                return -1;
            return 1;

        }
    };

    private static Comparator<org.opencv.core.Rect> yComparator = new Comparator<org.opencv.core.Rect>() {

        @Override
        public int compare(org.opencv.core.Rect rect1, org.opencv.core.Rect rect2) {
            if (rect1.y < rect2.y)
                return -1;
            return 1;

        }
    };


    public static Bitmap compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private static Point[] partArray(Point[] array, int size) {
        Point[] part = new Point[size];
        System.arraycopy(array, 0, part, 0, size);
        return part;
    }

    public static Bitmap changeBitmapColors(Bitmap bitmap) {

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);


        Bitmap result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(src, result);

        return result.copy(Bitmap.Config.ARGB_8888, true);
    }

    public static Bitmap cropDocument(Bitmap bitmap) {

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);

        Mat mask = src.clone();

        Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(1, 1)));
        Imgproc.erode(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(3, 3)));
        Imgproc.adaptiveThreshold(mask, mask, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 175, 40);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(355, 355)));


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mask.clone(), contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_TC89_KCOS);

        RotatedRect docRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));
        RotatedRect rotatedRect = null;
        MatOfPoint2f mat = null;
        MatOfPoint2f pointsMat = new MatOfPoint2f();

        for (int i = 0; i < contours.size(); i++) {
            mat = new MatOfPoint2f(contours.get(i).toArray());
            rotatedRect = Imgproc.minAreaRect(mat);
            if (rotatedRect.size.width > docRect.size.width && rotatedRect.size.height > docRect.size.height) {
                docRect = rotatedRect;
                pointsMat = mat;
            }
        }

        Imgproc.boxPoints(docRect, pointsMat);
        Point[] points = CVProcessor.sortPoints(partArray(pointsMat.toArray(), 4));
        int margin = (bitmap.getWidth() * 2) / 100;

        points[0].x = points[0].x - margin;
        points[1].x = points[1].x + margin;
        points[2].x = points[2].x + margin;
        points[3].x = points[3].x - margin;

        points[0].y = points[0].y - margin;
        points[1].y = points[1].y - margin;
        points[2].y = points[2].y + margin;
        points[3].y = points[3].y + margin;

        src = CVProcessor.fourPointTransform(src, points);

        //src = mask;

        Bitmap result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, result);

        return result;

    }

    public static Bitmap decodeToBitMap(byte[] data, int width, int heigth) {


        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, width,
                    heigth, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new android.graphics.Rect(0, 0, width, heigth),
                        100, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(
                        stream.toByteArray(), 0, stream.size());
                stream.close();
                return bmp;
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
        return null;
    }

    public static void resize(Mat src, int height, int width) {

        org.opencv.core.Size size = src.size();

        Imgproc.resize(src, src, new org.opencv.core.Size(width, height), 0.5, 0.5, Imgproc.INTER_CUBIC);

    }

    public static double resize(Mat src, int height) {

        org.opencv.core.Size size = src.size();
        int width = (int) (height * size.width / size.height);

        Imgproc.resize(src, src, new org.opencv.core.Size(width, height));

        return size.height / (double) height;
    }

    public static Bitmap cropImageInRectView(Bitmap mBitmap, CameraView cameraView, View viewFinderLayout, int rootHeight, int roottWidth) {

        Mat uncropped = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(mBitmap, uncropped);

        com.otaliastudios.cameraview.size.Size picSize=cameraView.getPictureSize();
        com.otaliastudios.cameraview.size.Size size=cameraView.getSnapshotSize();
        Preview preview = cameraView.getPreview();
        int camWidth=cameraView.getWidth();
        int camHeight=cameraView.getHeight();

        double rWidth=(double) picSize.getWidth()/(double) camWidth;
        double rHeight=(double) picSize.getHeight()/(double) camHeight;

        resize(uncropped, rootHeight-camHeight, camWidth);

        int cropAreaWidth=viewFinderLayout.getWidth();
        int cropAreaHeight=viewFinderLayout.getHeight();

        double widthB =viewFinderLayout.getWidth()*rWidth;
        double heightB = viewFinderLayout.getHeight()*rHeight;

        double x = (viewFinderLayout.getX()*rWidth);
        double y = viewFinderLayout.getY()*rHeight;

        int unWidth = uncropped.cols();
        int unHeight = uncropped.rows();

        org.opencv.core.Rect roi = new org.opencv.core.Rect((int) x, (int) y,
                (int) widthB, (int) heightB);

        Mat cropped = new Mat(uncropped, roi);

        //int scaledHeight=(int)(cropped.rows()+(cropped.rows()*resizeRatio));
        //resize(cropped,scaledHeight);

        Bitmap result = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cropped, result);

        return result;
    }


    public static Bitmap cropImageInRectView(Bitmap mBitmap, View viewFinderLayout, int rootHeight, int roottWidth) {


        Mat uncropped = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(mBitmap, uncropped);

        resize(uncropped, rootHeight, roottWidth);

        double widthB = viewFinderLayout.getWidth();
        double heightB = viewFinderLayout.getHeight();

        double x = viewFinderLayout.getX();
        double y = viewFinderLayout.getY();


        int unWidth = uncropped.cols();
        int unHeight = uncropped.rows();

        org.opencv.core.Rect roi = new org.opencv.core.Rect((int) x, (int) y,
                (int) widthB, (int) heightB);

        Mat cropped = new Mat(uncropped, roi);

        //int scaledHeight=(int)(cropped.rows()+(cropped.rows()*resizeRatio));
        //resize(cropped,scaledHeight);

        Bitmap result = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cropped, result);

        return result;
    }

    public static Bitmap cropImageInRectView(Bitmap mBitmap, View viewFinderLayout, View rootLayout) {


        Mat uncropped = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(mBitmap, uncropped);

        double rootWidth = (double) rootLayout.getWidth();
        double rootHeight = (double) rootLayout.getHeight();
        double imageWidth = (double) mBitmap.getWidth();
        double imageHeight = (double) mBitmap.getHeight();


        double widthB = -1;
        double heightB = -1;

        double x = -1;
        double y = -1;
        double viewFinderWidth = (double) viewFinderLayout.getWidth();
        double viewFinderHeight = (double) viewFinderLayout.getHeight();

        if (rootWidth > imageWidth) {
            double reducedRootWidth = rootWidth - imageWidth;
            double reducedRootHeight = rootHeight - imageHeight;

            double reducedRootWidthPercent = reducedRootWidth / rootWidth;
            double reducedRootHeightPercent = reducedRootHeight / rootHeight;

            reducedRootWidthPercent = reducedRootWidthPercent > 0.01 ? reducedRootWidthPercent - 0.01 : reducedRootWidthPercent;
            widthB = Math.rint(viewFinderWidth - (viewFinderWidth * reducedRootWidthPercent));
            heightB = (viewFinderHeight - (viewFinderHeight * reducedRootHeightPercent));
        } else {
            double increasedRootWidth = imageWidth - rootWidth;
            double increasedRootHeight = imageHeight - rootHeight;

            double increasedRootWidthPercent = increasedRootWidth / imageWidth;
            double increasedRootHeightPercent = increasedRootHeight / imageHeight;

            increasedRootHeightPercent = increasedRootHeightPercent > 0.1 ? increasedRootHeightPercent + 0.3 : increasedRootHeightPercent;


            widthB = (viewFinderWidth + (viewFinderWidth * increasedRootWidthPercent));
            heightB = Math.rint(viewFinderHeight + (viewFinderHeight * increasedRootHeightPercent));

        }

        x = (imageWidth - widthB) / 2;
        y = (imageHeight - heightB) / 2;

        org.opencv.core.Rect roi = new org.opencv.core.Rect((int) x, (int) y,
                (int) widthB, (int) heightB);

        Mat cropped = new Mat(uncropped, roi);

        Bitmap result = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cropped, result);

        return result;
    }


    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

}


