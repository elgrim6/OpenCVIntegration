package mz.bancounico.uocr.models;

import android.graphics.Rect;
import androidx.annotation.NonNull;

import com.google.android.gms.vision.text.Line;


/**
 * Created by Barros on 2/18/2018.
 */

public class PositionLine implements Comparable {

    private Rect boundingBox;
    private String value;

    public PositionLine(Line line) {
        boundingBox = line.getBoundingBox();
        value = line.getValue();
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        PositionLine line = (PositionLine) o;

        if (this.boundingBox.bottom > line.boundingBox.bottom)
            return 1;
        else if (this.boundingBox.bottom < line.boundingBox.bottom)
            return -1;
        return 0;
    }
}