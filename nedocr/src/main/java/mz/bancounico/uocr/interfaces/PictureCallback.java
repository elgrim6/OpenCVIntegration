package mz.bancounico.uocr.interfaces;

/**
 * Created by dds_unico on 12/6/18.
 */

/**
 * Callback interface used to supply image data from a photo capture.
 */



public interface PictureCallback {
    /**
     * Called when image data is available after a picture is taken.  The format of the data
     * is a jpeg binary.
     */
    void onPictureTaken(byte[] data);
}