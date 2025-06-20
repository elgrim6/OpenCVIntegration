package mz.bancounico.uocr.ui.fragments;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.gesture.Gesture;
import com.otaliastudios.cameraview.gesture.GestureAction;
import com.otaliastudios.cameraview.size.SizeSelector;
import com.otaliastudios.cameraview.size.SizeSelectors;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mz.bancounico.uandroidasync.TaskListener;
import mz.bancounico.uocr.R;
import mz.bancounico.uocr.ui.camera.OpticalCaptureActivity;
import mz.bancounico.uocr.utils.ImageUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class CameraViewFragment extends Fragment {

    public static OpticalCaptureActivity opticalCaptureActivity;
    public View rootView;
    public boolean isFrontSide = true;

    public static final String ARG_PARAM_SCAN_DOC="ARG_PARAM_SCAN_DOC";
    public boolean canScanDoc = true;

    View viewFinderLayout;
    CameraView cameraView;
    ImageButton scanButton;
    View rotationEffectLayout;
    View cropRootView;
    boolean isLandscape = false;
    TextView documentSideIndicatorTextView;
    TextView documentTypeIndicatorTextView;
    ImageView documentPositionGuideImageViewFront;
    ImageView passpportPositionGuideImageView;
    EasyFlipView easyFlipView;

    private Bitmap frontDocumentGuideBitmap;
    private Bitmap backDocumentGuideBitmap;

    private MediaPlayer bleepCameraRing;

    protected byte[] frontPicture;
    private AnimatorSet mSetDocumentRotateAnimator;
    private View scanBar;
    private int currentVolume=0;
    private ImageView documentPositionGuideImageViewBack;

    private boolean isTwoSidesDoc=true;
    private TextView passportSideIndicatorTextView;

    public enum DocumentType{
        ID("BILHETE DE IDENTIDADE"),
        Passport("PASSAPORTE"),
        Driving_Licence("CARTA DE CONDUÇÃO"),
        Dire("DIRE"),
        Others("OUTRO"),
        Others_TwoSides("OUTROS");

        String id;

        DocumentType(String id){
            this.id=id;
        }

        public String getId() {
            return id;
        }

        public static DocumentType getDocumentTypeFromId(String id){
            for(DocumentType documentType: DocumentType.values() ){
                if(documentType.id.equals(id)) return documentType;
            }
            return null;
        }
    }

    protected CameraView getCameraView() {
        return cameraView;
    }


    public CameraViewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bleepCameraRing = MediaPlayer.create(getContext(), R.raw.bleep_sound);
        bleepCameraRing.setVolume(0.3f,0.3f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_document_capture_view, container, false);
        cameraView = rootView.findViewById(R.id.camera);
        viewFinderLayout = rootView.findViewById(R.id.viewFinderConslayout);
        rotationEffectLayout = rootView.findViewById(R.id.rotationEffectLayout);
        cropRootView=rootView.findViewById(R.id.cropRootView);
        documentSideIndicatorTextView=rootView.findViewById(R.id.documentSideIndicatorTextView);
        passportSideIndicatorTextView=rootView.findViewById(R.id.passportSideIndicatorTextView);
        documentTypeIndicatorTextView=rootView.findViewById(R.id.documentTypeIndicatorTextView);
         easyFlipView= rootView.findViewById(R.id.easyFlipView);
        documentPositionGuideImageViewFront =rootView.findViewById(R.id.documentPositionGuideImageViewFront);
        documentPositionGuideImageViewBack =rootView.findViewById(R.id.documentPositionGuideImageViewBack);
        passpportPositionGuideImageView=rootView.findViewById(R.id.passportPositionGuideImageView);
        configCamera();

        //int width=rootView.getWidth();
       // initializeRotationViewTip(rotationEffectLayout);
        scanButton = rootView.findViewById(R.id.scanActionView);
        scanBar=rootView.findViewById(R.id.scanBar);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureAction();
            }
        });

        loadAnimations();
        hideScanAnimation();

        showPassportGuides(false);
        return rootView;
    }

    public abstract void captureAction();

    public abstract void returnResult();

    void clearFrameProcessors() {
        getCameraView().clearFrameProcessors();
    }

    void playCameraShoot() {
        bleepCameraRing.start();
    }

    void showRotationViewTip() {

        if(isTwoSidesDoc)
          easyFlipView.flipTheView();
    }

    void addFrameProcessor(FrameProcessor frameProcessor) {
        getCameraView().addFrameProcessor(frameProcessor);
    }


    private void loadAnimations() {
        mSetDocumentRotateAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.rotate_document_animator);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void configCamera() {


        cameraView.setLifecycleOwner(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cameraView.setFocusedByDefault(true);
        }

        cameraView.setPlaySounds(false);
        cameraView.playSoundEffect(R.raw.bleep_sound);
        cameraView.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS);
        //cameraView.setExposureCorrection(600f);



        SizeSelector previewWidth = SizeSelectors.minWidth(1980);
        SizeSelector previewHeigth = SizeSelectors.minHeight(1000);
        SizeSelector previewDimensions = SizeSelectors.and(previewWidth, previewHeigth);

        SizeSelector result = SizeSelectors.or(
                SizeSelectors.and(previewDimensions), // Try to match both constraints
                SizeSelectors.biggest() // If none is found, take the biggest
        );

        SizeSelector wd = SizeSelectors.maxWidth(2000);
        SizeSelector he = SizeSelectors.minWidth(1900);
        SizeSelector di = SizeSelectors.and(wd, he);

        SizeSelector re = SizeSelectors.or(
                SizeSelectors.and(di), // Try to match both constraints
                SizeSelectors.biggest() // If none is found, take the biggest
        );

//        cameraView.setPreviewStreamSize(result);
//        cameraView.setPictureSize(re);
        cropRootView.getLayoutParams().width=cameraView.getWidth();
        cropRootView.getLayoutParams().height=cameraView.getHeight();




        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                Bitmap b= BitmapFactory.decodeByteArray(result.getData(),0,result.getData().length);

                if(result.getRotation()==0 && result.getSize().getWidth()<result.getSize().getHeight()) b=ImageUtils.rotateImage(b,-90);

                int rootWidth=rootView.getWidth();

                int camWidth=cameraView.getWidth();
                b=ImageUtils.cropImageInRectView(b,viewFinderLayout, cropRootView.getHeight(),cropRootView.getWidth());


                CameraViewFragment.this.onPictureTaken(
                        ImageUtils.getCompressedByteArrayFromBitmap(b,100, Bitmap.CompressFormat.JPEG));
            }
        });
    }

    private void showPassportGuides(boolean canShow){
       if(canShow){
         easyFlipView.setVisibility(View.GONE);
          documentSideIndicatorTextView.setVisibility(View.GONE);
           passpportPositionGuideImageView.setVisibility(View.VISIBLE);
           passportSideIndicatorTextView.setVisibility(View.VISIBLE
           );
       }else{
           easyFlipView.setVisibility(View.VISIBLE);
           documentSideIndicatorTextView.setVisibility(View.VISIBLE);
           passpportPositionGuideImageView.setVisibility(View.GONE);
           passportSideIndicatorTextView.setVisibility(View.GONE);
       }
    }

    void configDocumentLayout(DocumentType documentType){
      documentTypeIndicatorTextView.setText(documentType.getId());
      switch (documentType){
          case ID:
              setDocumentBitmaps(R.mipmap.bi_moz_illustration, R.mipmap.bi_moz_illustration_back_side);
              break;
          case Passport:
              showPassportGuides(true);
              setDocumentBitmaps(R.mipmap.passport_moz_illustration);
              isTwoSidesDoc=false;
              break;
          case Driving_Licence:
              setDocumentBitmaps(R.mipmap.driver_licence_moz_illustration,R.mipmap.driver_licence_moz_illustration);
              isTwoSidesDoc=false;
              break;
          case Dire:
              setDocumentBitmaps(R.mipmap.dire_moz_illustration,R.mipmap.dire_moz_illustration_back_side);
              break;
          case Others:
              setDocumentBitmaps(R.mipmap.doc_illustration);
              isTwoSidesDoc=false;
              break;
          case Others_TwoSides:
              setDocumentBitmaps(R.mipmap.doc_illustration,R.mipmap.doc_illustration_back_side);
              break;
      }


    }

    void setDocumentBitmaps(int resId){
       setDocumentBitmaps(resId,-1);
    }

    void setDocumentBitmaps(int resId, int backResId){
        frontDocumentGuideBitmap= BitmapFactory.decodeResource(getResources(),resId);
        documentPositionGuideImageViewFront.setImageBitmap(frontDocumentGuideBitmap);
        if(backResId!=-1){
            backDocumentGuideBitmap=BitmapFactory.decodeResource(getResources(),backResId);
            documentPositionGuideImageViewBack.setImageBitmap(backDocumentGuideBitmap);
        }
    }


    public abstract void onPictureTaken(byte[] jpeg);

    public void takePicture(){
        cameraView.takePicture();
    }

    public void takePictureAsync(TaskListener listener){

        playCameraShoot();
        cameraView.clearCameraListeners();
        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                Bitmap b= BitmapFactory.decodeByteArray(result.getData(),0,result.getData().length);

                if(result.getRotation()==0 && result.getSize().getWidth()<result.getSize().getHeight()) b=ImageUtils.rotateImage(b,-90);

                int rootWidth=rootView.getWidth();

                b=ImageUtils.cropImageInRectView(b,viewFinderLayout, cropRootView.getHeight(),cropRootView.getWidth());


                CameraViewFragment.this.onPictureTaken(
                        ImageUtils.getCompressedByteArrayFromBitmap(b,100, Bitmap.CompressFormat.JPEG));

                listener.success(true);
            }
        });
        cameraView.takePicture();
    }


    public void muteCameraDefaultShutter(){
        AudioManager audioManager=(AudioManager) this.getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        currentVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,0,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes=new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build();
            bleepCameraRing.setAudioAttributes(audioAttributes );
        }
    }

    void showScanAnimation() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Animation animation = AnimationUtils.loadAnimation(getActivity(), isLandscape ? R.anim.scan_animation_land : R.anim.scan_animation_portrait);
                scanBar.setVisibility(View.VISIBLE);
                scanBar.startAnimation(animation);
            }
        });

    }

    void hideScanAnimation() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanBar.setVisibility(View.GONE);
                scanBar.clearAnimation();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void returnResult(Intent intent) {

        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface CustomAnimationListener {
        public void onAnimationEnd();
    }
}
