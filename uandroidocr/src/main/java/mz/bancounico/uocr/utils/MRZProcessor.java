package mz.bancounico.uocr.utils;



public class MRZProcessor {

    public static String correctMrz(String mrzText){

        mrzText = mrzText.replaceAll("(O)(\\d)","0$2");
        mrzText = mrzText.replaceAll(" ","");
        mrzText = mrzText.replaceAll("ZOO","Z00");
        mrzText = mrzText.replaceAll("ZO","Z0");
        mrzText = mrzText.replaceAll("HOZ|H0Z","MOZ");
        mrzText = mrzText.replaceAll("0Z","OZ");
        mrzText = mrzText.replaceAll("^BI|^0I|^DI|^OI","AI");

        return mrzText;
    }
}
