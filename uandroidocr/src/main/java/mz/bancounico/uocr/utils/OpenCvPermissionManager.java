package mz.bancounico.uocr.utils;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class OpenCvPermissionManager {

      public static boolean start(){
          if (!OpenCVLoader.initDebug()) {
              Log.d("OPENCV_LOADER","Erro");
              // Handle initialization error
             return  false;
          }else{
              Log.d("OPENCV_LOADER","Sucesso");
              return true;
          }
      }
}
