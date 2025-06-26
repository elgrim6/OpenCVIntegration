package mz.bancounico.uocr.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.google.android.gms.vision.barcode.Barcode;

import org.opencv.android.Utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import mz.bancounico.uocr.utils.CVProcessor;

/**
 * Created by dds_unico on 8/8/18.
 */
public class PhotoTask {

    private Object result;
    private byte[] photoData;
    private HashMap<String, String> notDetectedProperties;
    private SparseArray<Barcode> barcodes;

    public PhotoTask(byte[] photoData, Object result, HashMap<String, String> notDetectedProperties) {
        this.result = result;
        this.photoData = photoData;
        this.notDetectedProperties = notDetectedProperties;
    }

    public PhotoTask(SparseArray<Barcode> barcodes) {
        this.barcodes = barcodes;
    }

    public PhotoTask(byte[] data, HashMap<String, String> notDetectedProperties) {
        this.photoData = data;
        this.notDetectedProperties = notDetectedProperties;
    }

    public Object getResult() {
        return result;
    }


    public byte[] getPhotoData() {
        return photoData;
    }

    public SparseArray<Barcode> getBarcodes() {
        return barcodes;
    }

    public HashMap<String, String> getNotDetectedProperties() {
        return notDetectedProperties;
    }


    public void setResult(Object result) {
        this.result = result;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    public void setNotDetectedProperties(HashMap<String, String> notDetectedProperties) {
        this.notDetectedProperties = notDetectedProperties;
    }

    public void setBarcodes(SparseArray<Barcode> barcodes) {
        this.barcodes = barcodes;
    }
}
