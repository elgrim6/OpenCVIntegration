package mz.bancounico.uocr.ui.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import mz.bancounico.uocr.R;
import mz.bancounico.uocr.ui.fragments.BaseCaptureFragment;
import mz.bancounico.uocr.ui.fragments.RegexCaptureFragment;
import mz.bancounico.uocr.utils.OpenCvPermissionManager;


public final class OpticalCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OpticalCaptureActivity";

    public static final String AUTO_FOCUS = "AUTO_FOCUS";
    public static final String USE_FLASH = "USE_FLASH";


    public static final String FRONT_DOCUMENT_PATH = "";
    public static final String BACK_DOCUMENT_PATH = "";
    public static final String DOCUMENT_TYPE = "";

    public static final String RESULT_DOCUMENT = "RESULT_DOCUMENT";
    public static final String RESULT_REGEX = "RESULT_REGEX";
    public static final String RESULT_BAR_CODE = "RESULT_BAR_CODE";


    public static final String MODE_DOCUMENT = "MODE_DOCUMENT";
    public static final String MODE_REGEX = "MODE_REGEX";
    public static final String MODE_BAR_CODE = "MODE_BAR_CODE";
    public static final String DETECTION_MODE = "DETECTION_MODE";
    private static final int REQUEST_DOCUMENT_INFO = 0;

    public static byte[] lastTakenPicture;
    public static SparseArray<Barcode> lastBarcodesDetected;
    public static byte[] frontPicture;
    public static byte[] backPicture;
    private String regexExpression;
    private String propertyName;
    public static final String REGEX_EXPRESSION = "REGEX_EXPRESSION";
    public static final String REGEX_PROPERTY_NAME = "REGEX_PROPERTY_NAME";
    public static final String DOCUMENT_SELECTED = "DOCUMENT_SELECTED";
    private boolean isDocumentTypeSelected = false;

    private BaseCaptureFragment captureFragment;


    public static Bitmap frontPictureBitmap;
    public static Bitmap backPictureBitmap;

    private View identityCardButton;
    private View passportCardButton;
    private View direCardButton;
    private View driverLicenseCardButton;
    private View otherOneSideCardButton;
    private View otherTwoSidesCardButton;
    private Fragment currentFragment;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_capture);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(" Escolha o tipo de Documento");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        identityCardButton = findViewById(R.id.identityCardButton);
        passportCardButton = findViewById(R.id.passportButton);
        direCardButton = findViewById(R.id.direButton);
        driverLicenseCardButton = findViewById(R.id.driverLicenseButton);
        otherOneSideCardButton = findViewById(R.id.othersFront);
        otherTwoSidesCardButton = findViewById(R.id.otherFrontVerse);

        String s = getIntent().getStringExtra(DETECTION_MODE);

        if (getIntent().getStringExtra(DETECTION_MODE) != null) {

            if (getIntent().getStringExtra(DETECTION_MODE).equals(MODE_REGEX)) {

                openRegexDetection();
            } else {
                if (getIntent().getStringExtra(DETECTION_MODE).equals(MODE_BAR_CODE)) {
                    //showDocumentTypesLayout(false);
                    openBarCodeDetection();
                }
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!OpenCvPermissionManager.start()) {
            Toast.makeText(this, "Não foi possivel carregar todas bibliotecas do OCR", Toast.LENGTH_LONG).show();
        }
    }

    public static void setImagesBitmap(Bitmap frontPictureP, Bitmap backPictureP) {
        frontPictureBitmap = frontPictureP;
        backPictureBitmap = backPictureP;
    }


    public static void setImages(byte[] frontPictureP, byte[] backPictureP) {
        frontPicture = frontPictureP;
        backPicture = backPictureP;
    }

    public void openRegexDetection() {
        regexExpression = getIntent().getStringExtra(REGEX_EXPRESSION);
        propertyName = getIntent().getStringExtra(REGEX_PROPERTY_NAME);

        captureFragment = new RegexCaptureFragment();
        ((RegexCaptureFragment) captureFragment).setRegexExpression(regexExpression);
        ((RegexCaptureFragment) captureFragment).setPropertyName(propertyName);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainViewContent, captureFragment).commit();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void openBarCodeDetection() {
        //captureFragment = new BarcodeCaptureFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.mainViewContent, captureFragment).commit();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DOCUMENT_SELECTED, isDocumentTypeSelected);
    }

    public void openCameraForDocument(String documentType) {
        Intent intent = new Intent(OpticalCaptureActivity.this, DocumentCaptureActivity.class);
        intent.putExtra(DocumentCaptureActivity.DOCUMENT_TYPE, documentType);
        startActivityForResult(intent, REQUEST_DOCUMENT_INFO);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //getSupportFragmentManager().beginTransaction().de(currentFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //getSupportFragmentManager().beginTransaction().detach(currentFragment);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DOCUMENT_INFO) {
            if (resultCode == AppCompatActivity.RESULT_OK) {

                Intent intent = getIntent();
                intent.putExtra(OpticalCaptureActivity.RESULT_DOCUMENT, data.getSerializableExtra(OpticalCaptureActivity.RESULT_DOCUMENT));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }


}


