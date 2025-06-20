package mz.bancounico.uocr.ui.camera;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import mz.bancounico.uocr.R;
import mz.bancounico.uocr.ui.fragments.DireCaptureFragment2;
import mz.bancounico.uocr.ui.fragments.DriverLicenseCaptureFragment;
import mz.bancounico.uocr.ui.fragments.DriverLicenseCaptureFragment2;
import mz.bancounico.uocr.ui.fragments.IDMRZCaptureFragment;
import mz.bancounico.uocr.ui.fragments.OtherOneSideCaptureFragment;
import mz.bancounico.uocr.ui.fragments.OtherTwoSidesCaptureFragment;
import mz.bancounico.uocr.ui.fragments.PassportMRZCaptureFragment;

public class DocumentCaptureActivity extends LandscapeActivity {


    public static final String DOCUMENT_TYPE = "DOCUMENT_TYPE";
    public static final String DOCUMENT_ID = "DOCUMENT_ID";
    public static final String DOCUMENT_DIRE = "DOCUMENT_DIRE";
    public static final String DOCUMENT_PASSPORT = "DOCUMENT_PASSPORT";
    public static final String DOCUMENT_DRIVER_LICENSE = "DOCUMENT_DRIVER_LICENSE";
    public static final String DOCUMENT_OTHER_ONE_SIDE = "DOCUMENT_OTHER_ONE_SIDE";
    public static final String DOCUMENT_OTHER_TWO_SIDES = "DOCUMENT_OTHER_TWO_SIDES";
    public static final String EXTRA_CAN_SCAN_DOC = "EXTRA_CAN_SCAN_DOC";
    private boolean canScanDoc=true;
    private Fragment captureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean canScanDoc=getIntent().getBooleanExtra(EXTRA_CAN_SCAN_DOC, true);




        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fragment_capture);

        String documentType = getIntent().getStringExtra(DOCUMENT_TYPE);

        if (documentType != null) {
            switch (documentType) {
                case DOCUMENT_ID:
                    captureFragment = new IDMRZCaptureFragment().newInstance(canScanDoc);
                    break;
                case DOCUMENT_DIRE:
                    captureFragment = new DireCaptureFragment2().newInstance(canScanDoc);
                    break;
                case DOCUMENT_PASSPORT:
                    captureFragment = new PassportMRZCaptureFragment().newInstance(canScanDoc);
                    break;
                case DOCUMENT_DRIVER_LICENSE:
                    captureFragment = new DriverLicenseCaptureFragment2().newInstance(canScanDoc);
                    break;

                case DOCUMENT_OTHER_ONE_SIDE:
                    captureFragment = new OtherOneSideCaptureFragment();
                    break;

                case DOCUMENT_OTHER_TWO_SIDES:
                    captureFragment = new OtherTwoSidesCaptureFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.mainViewContent, captureFragment).commit();
        }

    }
}
