package mz.bancounico.uocr.logic;

import com.google.android.gms.common.images.Size;

/**
 * Created by dds_unico on 12/6/18.
 */

public class SizePair {
    private Size mPreview;
    private Size mPicture;

    public SizePair(android.hardware.Camera.Size previewSize,
                    android.hardware.Camera.Size pictureSize) {
        mPreview = new Size(previewSize.width, previewSize.height);
        if (pictureSize != null) {
            mPicture = new Size(pictureSize.width, pictureSize.height);
        }
    }

    public Size previewSize() {
        return mPreview;
    }

    @SuppressWarnings("unused")
    public Size pictureSize() {
        return mPicture;
    }
}