package mz.bancounico.uocr.utils;

import com.googlecode.tesseract.android.TessBaseAPI;

public class Tess {

    private TessBaseAPI tessBaseapi;


    public static TessBaseAPI getNewInstance(){

        TessBaseAPI tessBaseapi = new TessBaseAPI();
        tessBaseapi.init(TesseractResourceManager.tesspath, "eng");
        setTessVariable(tessBaseapi, "load_system_dawg", "0");
        setTessVariable(tessBaseapi, "load_freq_dawg", "0");
        setTessVariable(tessBaseapi, "load_unambig_dawg", "0");
        setTessVariable(tessBaseapi, "load_punc_dawg", "0");
        setTessVariable(tessBaseapi, "load_number_dawg", "0");
        setTessVariable(tessBaseapi, "load_fixed_length_dawgs", "0");
        setTessVariable(tessBaseapi, "load_bigram_dawg", "0");
        setTessVariable(tessBaseapi, "wordrec_enable_assoc", "0");
        setTessVariable(tessBaseapi, "tessedit_enable_bigram_correction", "0");
        setTessVariable(tessBaseapi, "assume_fixed_pitch_char_segment", "1");
        setTessVariable(tessBaseapi, TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ<");
        return tessBaseapi;
    }

    public TessBaseAPI getBaseApi(){
        return tessBaseapi;
    }


    private static void setTessVariable(TessBaseAPI tessBaseApi, String var, String value) {
        tessBaseApi.setVariable(var, value);
    }

}
