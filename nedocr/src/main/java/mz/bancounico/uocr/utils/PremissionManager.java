package mz.bancounico.uocr.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import static android.content.ContentValues.TAG;

/**
 * Created by dds_unico on 8/29/18.
 */

public class PremissionManager {


    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_GMS = 9001;

    Activity context;

    public PremissionManager(Activity context) {
        this.context = context;
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(context,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(context, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }


        new AlertDialog.Builder(context)
                .setTitle("Camera Permission Required")
                .setMessage("access to device camera is required for scanning")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(context, permissions,
                                RC_HANDLE_CAMERA_PERM);
                    }
                }).show();
    }


    public void checkDevicePlayServicesAvailable() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                context);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(context, code, RC_HANDLE_GMS);
            dlg.show();
        } else {
            //createCameraSource();
            //startCameraSource();
        }
    }

    public void checkCameraPermission() {
        int rc = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            checkDevicePlayServicesAvailable();
        } else {
            requestCameraPermission();
        }
    }

}
