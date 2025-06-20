package mz.bancounico.uocr.detectors;

import android.content.Context;

/**
 * Created by dds_unico on 3/9/18.
 */



public class RegexTextDetector extends TextDetector {
    private String regexExpression;
    private String PROPERTY = "PROPERTY";
    private String propertyName;
    private String result;

    public RegexTextDetector(Context context, String regexExpression, String propertyName) {
        super(context);
        this.propertyName = propertyName;
        getNotDetectedProperties().put(PROPERTY, propertyName);
        this.regexExpression = regexExpression;
        result = "";
    }

    @Override
    public Object getData() {

        if (getNotDetectedProperties().get(PROPERTY) != null) {
            result = getResult();
        }
        clearLines();
        return result;
    }


    public String getResult(){
        int position = find(regexExpression);

        if(position == -1) {
            return "";
        }
        getNotDetectedProperties().remove(PROPERTY);
        return lines.get(position).getValue().replaceAll("\\s","");
    }

    @Override
    public void setParams() {


    }
}
