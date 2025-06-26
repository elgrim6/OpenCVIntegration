package mz.bancounico.uocr.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mz.bancounico.uocr.R;

public class TesseractResourceManager {


    public static final String tesspath = Environment.getExternalStorageDirectory() + File.separator + "tesseract";
    private Context mContext = null;
    private static volatile TesseractResourceManager singleton = null;

    public TesseractResourceManager(Context context) {
        this.mContext=context;
        saveTessDataInPath(tesspath);
    }


    private void saveTessDataInPath(String datapath)  {

        if(mContext !=null) {
            InputStream is = mContext.getResources().openRawResource(R.raw.eng);
            // Create the folder and save file
            File folder=new File(datapath+File.separator+"tessdata");
            File file = new File(folder,"eng.traineddata");
            if(isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
                if(!file.exists()) {
                    if (!folder.mkdirs()) {
                        Log.e("FILE_TESS_DATA", "Directory not createe");
                    }
                    copyInputStreamToFile(is,file);
                }
            }
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;

    }


    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[4*1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
}
