package mz.bancounico.uocr.ui.camera;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import mz.bancounico.uocr.utils.OpenCvPermissionManager;

public class LandscapeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCvPermissionManager.start();
    }
}
