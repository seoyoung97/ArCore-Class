package com.example.ex10_googlemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Earth;
import com.google.ar.core.Frame;

import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;

import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;



public class MainActivity extends AppCompatActivity {
    Session mSession;

    GLSurfaceView mySurfaceView;
    TextView myTextView;
    MapTouchWrapper mapTouchWrapper;

    MyMapFrrr myMapFrrr;

    MainRenderer mRenderer;

    float displayX, displayY, mRotate = 0f, mScale = 1f;

    boolean mTouched = false, modelInit = false, moving = false;

    //이동 회전-> 한 손가락 이벤트
    GestureDetector mGestureDetector;

    //크기조절 -> 두 손가락 이벤트트
    ScaleGestureDetector mScaleGestureDetector;

    float [] modelMatrix = new float[16];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hidStatusBarTitleBar();
        setContentView(R.layout.activity_main);

        mySurfaceView = (GLSurfaceView)findViewById(R.id.glSsurfaceview);
        myTextView = (TextView)findViewById(R.id.myTextView);
        mapTouchWrapper = (MapTouchWrapper)findViewById(R.id.mymap_wrapper);

        SupportMapFragment mapFragment =
                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mymap);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                myMapFrrr = new MyMapFrrr(MainActivity.this, googleMap);
            }
        });

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

            //이동
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                mTouched = true;
                modelInit = false;
                displayX= event.getX();
                displayY = event.getY();
                Log.d("onDoubleTap 여",displayX+","+displayY);
                return true;
            }

            class MyGo extends Thread{

                int code;

                MyGo(float distance){
                    code = distance < 0 ? 1 : -1;
                }

                @Override
                public void run() {

                    if(!moving) {
                        moving = true;
                        float[] bufMatirx = modelMatrix.clone();

                        for (int i = 0; i < 200; i++) {
                            Matrix.translateM(modelMatrix, 0, 0, 0, 0.05f*code);
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        modelMatrix = bufMatirx;
                        moving = false;
                    }
                }
            }

            //회전
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                if(modelInit && !moving) {

                    if(distanceY >-50 && distanceY <50) {
                        mRotate += -distanceX / 5;
                        Matrix.rotateM(modelMatrix, 0, -distanceX / 5, 0f, 100f, 0f);
                    }else{
                        new MyGo(distanceY).start();
                    }

                }

               // Log.d("onScroll 여", distanceX + "," + distanceY);
                return true;
            }
        });






        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                Log.d("onScale 여",detector.getScaleFactor()+"");

                mScale *= detector.getScaleFactor();
                Matrix.scaleM(modelMatrix,0,
                        detector.getScaleFactor(),
                        detector.getScaleFactor(),
                        detector.getScaleFactor());
                return true;
            }
        });



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


                Earth earth = mSession.getEarth();

                if(earth.getTrackingState() == TrackingState.TRACKING){

                    GeospatialPose mePose = earth.getCameraGeospatialPose();
                    //Log.d("mePose 여",mePose.getLatitude()+","+mePose.getLongitude()+","+mePose.getHeading());

                     /*
                    mePose.getLatitude(), //경도
                            mePose.getLongitude(), // 경도
                            mePose.getHorizontalAccuracy(), // 수평정확도
                            mePose.getAltitude(), // 고도
                            mePose.getVerticalAccuracy(), // 수직 정확도
                            mePose.getHeading(), // 각도

                    */

                    runOnUiThread(() -> {
                        myTextView.setText(
                                "위도-> " + mePose.getLatitude() + "\n" + "경도-> " + mePose.getLongitude() + "\n" +
                                        "수평 정확도->  " + mePose.getHorizontalAccuracy() + "\n" + "고도-> " + mePose.getAltitude() + "\n" +
                                        "수직 정확도-> " + mePose.getVerticalAccuracy() + "\n"+ "각도-> " + mePose.getHeading() + "\n"
                        );
                    });

                    if(myMapFrrr != null){

                        myMapFrrr.updateMapMe(mePose.getLatitude(),mePose.getLongitude(),mePose.getHeading());
                    }


                }




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
                        //config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);

                        //지리정보 데이터 사용 가능상태로 변경
                        config.setGeospatialMode(Config.GeospatialMode.ENABLED);
                        mSession.configure(config);

                        Log.d("세션 생성?","생성됐으요");
                        break;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }



        }


        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mySurfaceView.onResume();


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
                    new String[]{ Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event); //이벤트 위임임
        mScaleGestureDetector.onTouchEvent(event);


        return true;
    }



}

        /*
                    mePose.getLatitude(),
                            mePose.getLongitude(),
                            mePose.getHorizontalAccuracy(),
                            mePose.getAltitude(),
                            mePose.getVerticalAccuracy(),
                            mePose.getHeading(),

                    */