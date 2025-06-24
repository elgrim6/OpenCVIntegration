package mz.bancounico.uocr.ui.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import mz.bancounico.uocr.R;
import mz.bancounico.uocr.ui.camera.DocumentCaptureActivity;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;

public class DocumentTypeChooserDialogFragment extends DialogFragment {

    private static final String ARG_PARAM_SCAN_DOC="ARG_PARAM_SCAN_DOC";
    private int REQUEST_CODE = -1;
    private boolean CAN_SCAN_DOC=true;
    private static final String ARG_PARAM_REQUEST_CODE = "ARG_PARAM_REQUEST_CODE";
    private View identityCardButton;
    private View passportCardButton;
    private View direCardButton;
    private View driverLicenseCardButton;
    private View otherOneSideCardButton;
    private View otherTwoSidesCardButton;

    public static DocumentTypeChooserDialogFragment newInstance(int requestCode, boolean canScanDocument) {
        DocumentTypeChooserDialogFragment fragment = new DocumentTypeChooserDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_REQUEST_CODE, requestCode);
        args.putBoolean(ARG_PARAM_SCAN_DOC,canScanDocument);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            REQUEST_CODE = getArguments().getInt(ARG_PARAM_REQUEST_CODE);
            CAN_SCAN_DOC = getArguments().getBoolean(ARG_PARAM_SCAN_DOC);
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppThemeCustomDialog);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_fragment_document_type_chooser, container, false);

        identityCardButton = view.findViewById(R.id.identityCardButton);
        passportCardButton = view.findViewById(R.id.passportButton);
        direCardButton = view.findViewById(R.id.direButton);
        driverLicenseCardButton = view.findViewById(R.id.driverLicenseButton);
        otherOneSideCardButton = view.findViewById(R.id.othersFront);
        otherTwoSidesCardButton = view.findViewById(R.id.otherFrontVerse);

        getDialog().setTitle("Tipo de Documento");
        setDocumentTypesCardButtonsListener();

        return view;
    }

    private void openCameraForDocument(String documentType) {
        Intent intent = new Intent(getActivity(), DocumentCaptureActivity.class);
        intent.putExtra(OpticalCaptureActivity.DETECTION_MODE, OpticalCaptureActivity.MODE_DOCUMENT);
        intent.putExtra(DocumentCaptureActivity.DOCUMENT_TYPE, documentType);
        intent.putExtra(DocumentCaptureActivity.EXTRA_CAN_SCAN_DOC, CAN_SCAN_DOC);
        getActivity().startActivityForResult(intent, REQUEST_CODE);
        dismiss();
    }


    private void setDocumentTypesCardButtonsListener() {

        identityCardButton.setOnClickListener(view -> {
            openCameraForDocument(DocumentCaptureActivity.DOCUMENT_ID);
        });
        passportCardButton.setOnClickListener(view -> {
            openCameraForDocument(DocumentCaptureActivity.DOCUMENT_PASSPORT);
        });
        direCardButton.setOnClickListener(view -> {
            openCameraForDocument(DocumentCaptureActivity.DOCUMENT_DIRE);
        });
        otherOneSideCardButton.setOnClickListener(view -> {
            openCameraForDocument(DocumentCaptureActivity.DOCUMENT_OTHER_ONE_SIDE);
        });

        otherTwoSidesCardButton.setOnClickListener(view -> {
            openCameraForDocument(DocumentCaptureActivity.DOCUMENT_OTHER_TWO_SIDES);
        });

        driverLicenseCardButton.setOnClickListener(view -> {
            openCameraForDocument(DocumentCaptureActivity.DOCUMENT_DRIVER_LICENSE);
        });

    }

}
