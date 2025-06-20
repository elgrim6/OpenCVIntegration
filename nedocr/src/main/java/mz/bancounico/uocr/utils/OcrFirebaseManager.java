package mz.bancounico.uocr.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;

public class OcrFirebaseManager {

    public static void init(Context context){
        FirebaseApp.initializeApp(context);
    }
}
