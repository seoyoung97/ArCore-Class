package com.example.ex08_image;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;

import com.google.ar.core.Pose;
import com.google.ar.core.Session;

import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;


public class MainActivity extends AppCompatActivity {
    Session mSession;

    GLSurfaceView mySurfaceView;

    TextView myTextView;

    MainRenderer mRenderer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hidStatusBarTitleBar();
        setContentView(R.layout.activity_main);

        mySurfaceView = (GLSurfaceView)findViewById(R.id.glSsurfaceview);
        myTextView = (TextView)findViewById(R.id.myTextView);

        //화면 변화 감지 --> 회전 등등
        DisplayManager displayManager = (DisplayManager)getSystemService(DISPLAY_SERVICE);

        if(displayManager != null){

            //화면 리스너 실행
            displayManager.registerDisplayListener(
                    new DisplayManager.DisplayListener() {
                        @Override
                        public void onDisplayAdded(int displayId) {

                        }

                        @Override
                        public void onDisplayRemoved(int displayId) {

                        }

                        //화면이 변경되었다면
                        @Override
                        public void onDisplayChanged(int displayId) {
                            //동기화 --> 변환시 한번되고 난뒤에 작업
                            synchronized (this){
                                //화면 변화를 알려준다
                                mRenderer.onDisplayChanged();
                            }

                        }
                    }, null);
        }



        MainRenderer.RenderCallBack mr = new MainRenderer.RenderCallBack() {

            //MainRenderer의 onDrawFrame() -- 그리기 할때 마다 호출
            //MainActivity에서 카메라 화면 정보를 얻기 위해서 이다.
            @Override
            public void preRender() {

                if(mRenderer.viewportChange){
                    Display display = getWindowManager().getDefaultDisplay();

                    mRenderer.updateSession(mSession, display.getRotation());
                }


                //session의 카메라 텍스처 이름을  mainRenderer의 카메라의 텍스처 번호로 지정
                // session 카메라 : 입력정보  --> mainRenderer의 카메라 :  화면에 뿌리는 출력정보
                mSession.setCameraTextureName(mRenderer.getTextureID());

                Frame frame = null;

                try {
                    frame = mSession.update(); //카메라의 화면을 업데이트한다.
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                mRenderer.mCamera.transformDisplayGeometry(frame);


                Camera camera = frame.getCamera();

                float [] viewMatrix = new float[16];
                float [] projMatrix = new float[16];

                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);
                camera.getViewMatrix(viewMatrix, 0);


                drawImages(frame);

                mRenderer.updateProjMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
                //Log.d("MainActivity 여","preRender() 여");
            }
        };

        mRenderer = new MainRenderer(mr, this);

        //pause 시 관련 데이터 사라지지 않게 한다.
        mySurfaceView.setPreserveEGLContextOnPause(true);
        mySurfaceView.setEGLContextClientVersion(3); //버전 3.0 사용

        //렌더링 지정
        mySurfaceView.setRenderer(mRenderer);
        //렌더링 계속 호출
        mySurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //퍼미션 요청
        requestPermission();

        //ARCore session 유무 --> 없으면 생성
        if(mSession == null){

            try {

                Log.d("세션 돼냐?",
                        ArCoreApk.getInstance().requestInstall(this, true)+"");

                switch (ArCoreApk.getInstance().requestInstall(this, true)){
                    case INSTALLED:
                        //ARCore  정상 설치후 세션 생성
                        mSession = new Session(this);

                        //ARCore 환경설정용 Config
                        Config config = new Config(mSession);
                        //평면 배치 인식
                        config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
                        mSession.configure(config);

                        Log.d("세션 생성?","생성됐으요");
                        break;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        Config mConfig = new Config(mSession);

        setImgDB(mConfig);

        mSession.configure(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mySurfaceView.onResume();


    }

    //이미지DB 설정
    void setImgDB(Config mConfig){
        //이미지데이터베이스 생성
        AugmentedImageDatabase imgDB = new AugmentedImageDatabase(mSession);

        try {
            InputStream is = getAssets().open("bbb.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imgDB.addImage("zzxx",bitmap);
            is.close();

            is = getAssets().open("qr.png");
            bitmap = BitmapFactory.decodeStream(is);
            imgDB.addImage("aass",bitmap);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //이미지 데이터베이스 설정
        mConfig.setAugmentedImageDatabase(imgDB);
    }

    //이미지 추적하여 객체 위치 설정정
   void drawImages(Frame frame){

        Collection<AugmentedImage> agImgs = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage img: agImgs){

            if(img.getTrackingState()==TrackingState.TRACKING) {

                Pose pose = img.getCenterPose();



                switch (img.getName()){
                    case "zzxx":
                        Log.d("drawImages1 여", img.getIndex() +img.getName()+
                                pose.tx()+","+
                                pose.ty()+","+
                                pose.tz());

                        float [] modelMatrix = new float[16];
                        pose.toMatrix(modelMatrix,0);
                        mRenderer.mObj_1.setModelMatrix(modelMatrix);
                        break;
                    case "aass":
                        Log.d("drawImages2 여", img.getIndex() +img.getName()+
                                pose.tx()+","+
                                pose.ty()+","+
                                pose.tz());

                        float [] modelMatrix2 = new float[16];
                        pose.toMatrix(modelMatrix2,0);
                        Matrix.scaleM(modelMatrix2,0,0.001f,0.001f,0.001f);
                        mRenderer.mObj_2.setModelMatrix(modelMatrix2);
                        break;
                }

            }
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        mySurfaceView.onPause();
        mSession.pause();
    }

    void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.CAMERA},
                    0
            );
        }
    }

    void hidStatusBarTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }
}