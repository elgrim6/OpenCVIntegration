package mz.bancounico.uocr.detectors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import mz.bancounico.uocr.models.PositionLine;


/**
 * Created by dds_unico on 3/9/18.
 */

public abstract class TextDetector extends Detector {

    public List<PositionLine> lines;
    public int errorMargin;
    private Context context;
    private int maxLines;
    private int minLines;

    private HashMap<String,String> notDetectedProperties;
    private HashMap<String,String> notDetectedFriendlyProperties;


    public TextDetector(Context context) {
        this.context = context;
        this.lines = new ArrayList<>();
        notDetectedProperties = new HashMap<>();
        notDetectedFriendlyProperties = new HashMap<>();
        errorMargin = 0;
    }


    public HashMap<String, String> getNotDetectedProperties() {
        return notDetectedProperties;
    }

    public HashMap<String, String> getNotFriendlyDetectedProperties() {
        return notDetectedFriendlyProperties;
    }

    public void setNotDetectedProperties(HashMap<String, String> notDetectedProperties) {
        this.notDetectedProperties = notDetectedProperties;
    }

    public Context getContext() {
        return context;
    }


    public abstract void setParams();


    public String getLineValue(int position) {

        if (lines.size() > position)
            return lines.get(position).getValue().toUpperCase();
        return null;
    }

    public int getTextLines(Bitmap imageBitmap) {

        if (imageBitmap != null) {
            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
            if (!textRecognizer.isOperational()) {
                // Note: The first time that an app using a Vision API is installed on a
                // device, GMS will download a native libraries to the device in order to do detection.
                // Usually this completes before the app is run for the first time.  But if that
                // download has not yet completed, then the above call will not detect any text,
                // barcodes, or faces.
                // isOperational() can be used to check if the required native libraries are currently
                // available.  The detectors will automatically become operational once the library
                // downloads complete on device.
                Log.w("Teste", "Detector dependencies are not yet available.");
                //Toast.makeText(context,"Detector dependencies are not yet available",Toast.LENGTH_SHORT).show();

                // checkDigit for low storage.  If there is low storage, the native library will not be
                // downloaded, so detection will not become operational.
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = context.registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(context, "Low Storage", Toast.LENGTH_LONG).show();
                    Log.w("Teste", "Low Storage");
                }
            }

            Frame imageFrame = new Frame.Builder()
                    .setBitmap(imageBitmap)
                    .build();

            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);


            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                for (int j = 0; j < textBlock.getComponents().size(); j++) {
                    orderLine((Line) textBlock.getComponents().get(j));
                    this.lines.add(new PositionLine((Line) textBlock.getComponents().get(j)));

                }
                //Log.e("Linha", "texto " + lines.get(i).getValue());
            }

        }

        return lines.size();
    }


    public void setLines(SparseArray<TextBlock> textBlocks) {

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            for (int j = 0; j < textBlock.getComponents().size(); j++) {
                orderLine((Line) textBlock.getComponents().get(j));
                this.lines.add(new PositionLine((Line) textBlock.getComponents().get(j)));
            }
        }
        orderLines();
    }

    public void orderLines() {
        Collections.sort(lines);
    }

    public void orderLine(Line line) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            line.getComponents().sort(new Comparator<Text>() {
                @Override
                public int compare(Text text, Text t1) {

                    if (text.getBoundingBox().right > t1.getBoundingBox().right)
                        return 1;
                    else if (text.getBoundingBox().right < t1.getBoundingBox().right)
                        return -1;

                    return 0;
                }
            });
        }
    }



    // Find using regular expressions
    // remove spaces to detct things like 899 899 898

    public int find(String regex) {
       return find(regex,0,true);
    }

    public int find(String regex, int startPosition, boolean upperCase){
        int position = -1;
        Pattern p = Pattern.compile(regex);
        for (int i = startPosition; i < lines.size(); i++) {
            String line;
            if(upperCase)
                line = lines.get(i).getValue().replaceAll("\\s","").toUpperCase();
            else
                line = lines.get(i).getValue().replaceAll("\\s","");

            if (p.matcher(line).find()) {
                position = i;
                break;
            }
        }
        return position;
    }

    public int find(String regex, int startPosition){
        return find(regex,startPosition,true);
    }

    public int findNonUpperCase(String regex) {
        return find(regex,0,false);
    }

    @Override
    public boolean hasDetect() {
        if (getNotDetectedProperties().size() > 0)
            return false;
        return true;
    }

    public boolean find(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        if (p.matcher(text.toUpperCase()).find()) {
            return true;
        }
        return false;
    }


    public int find(int size) {
        int position = -1;
        for (int i = 0; i > lines.size(); i++) {
            if (lines.get(i).getValue().length() == size)
                position = i;
        }
        return position;
    }


    public void clearLines() {
        lines.clear();
    }


}
