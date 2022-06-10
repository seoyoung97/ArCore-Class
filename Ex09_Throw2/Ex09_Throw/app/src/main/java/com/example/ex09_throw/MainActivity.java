package com.example.ex09_throw;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Session mSession;

    GLSurfaceView mySurfaceView;

    TextView myTextView;

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

                Frame frame = null;

                try {
                    frame = mSession.update(); //카메라의 화면을 업데이트한다.
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                mRenderer.mCamera.transformDisplayGeometry(frame);


                if(mTouched) {


                    LightEstimate estimate = frame.getLightEstimate();
                    float lightyIntensity = estimate.getPixelIntensity();
                    //Log.d("빛의 세기 여",lightyIntensity+"");

                    //List<HitResult> results = frame.hitTestInstantPlacement(displayX, displayY, 2.0f);
                    List<HitResult> results = frame.hitTest(displayX, displayY);
                    for (HitResult hr : results) {
                        Pose pose = hr.getHitPose();
//                        InstantPlacementPoint point = (InstantPlacementPoint) hr.getTrackable();
//                        Log.d("hitTestInstantPlacement 여", pose.tx() + "," + pose.ty() + "," + pose.tz());

                        Trackable trackable = hr.getTrackable();

                        //클릭좌표추적이  Plane  이고  Plane의 도형 안에 있어?
                        if(trackable instanceof Plane &&  ((Plane)trackable).isPoseInPolygon(pose)){


                            if(!modelInit){
                                modelInit = true;

                                float [] cubeMatrix = new float[16];
                                pose.toMatrix(modelMatrix,0);
                                pose.toMatrix(cubeMatrix,0);

                                float zz = ((float)Math.random()*4+1) * ((int)(Math.random()*2) > 0 ? 1: -1);
                                float xx = ((float)Math.random()*6-3);

                                zz = -4f;
                                xx = 0f;



                                Matrix.translateM(cubeMatrix, 0, xx, 0, zz);

                                Matrix.rotateM(modelMatrix, 0, mRotate, 0f, 100f, 0f);
                                Matrix.scaleM(modelMatrix,0,
                                        mScale, mScale, mScale);


                                mRenderer.mCube.setModelMatrix(cubeMatrix);

                            }


                           // Matrix.translateM(modelMatrix,0,0f,0.0f,0f);
                           // Matrix.rotateM(modelMatrix,0, 45f,0f,100f,0f);
                           // Matrix.scaleM(modelMatrix,0,1f,3f,1f);

                            mRenderer.mObj.setLightIntensity(lightyIntensity);

                            float [] colorArr = {1f,1f,1f,0.6f};

                            mRenderer.mObj.setColorCorrection(colorArr);
                            mRenderer.mObj.setModelMatrix(modelMatrix);


                            if(moving){
                                catchCheck();
                            }
                          //  Log.d("터치 여","obj 그린다.");
                        }
                    }
                   // mTouched =false;
                }



                Collection<Plane> planes = mSession.getAllTrackables(Plane.class);

                for (Plane plane: planes) {
                    if(plane.getTrackingState() == TrackingState.TRACKING && plane.getSubsumedBy() == null) {
                        mRenderer.mPlane.update(plane);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myTextView.setText("평면 찾음요");
                            }
                        });

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myTextView.setText("아직도요");
                            }
                        });
                    }

                }


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
                        config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event); //이벤트 위임임
        mScaleGestureDetector.onTouchEvent(event);
       /* displayX= event.getX();
        displayY = event.getY();

        if(event.getAction()==MotionEvent.ACTION_MOVE) {
            Log.d("onTouchEvent 여", event.getAction() + "");


        }
        mTouched = true;*/

        return true;
    }


    void catchCheck(){

        float [][]  cubePP = mRenderer.mCube.getPoint();
        float [][]  objPP = mRenderer.mObj.getPoint();


        //Log.d("cubePP 여", Arrays.toString(cubePP[0]) + "");
        //Log.d("objPP 여", Arrays.toString(objPP[0]) + "");

        if(cubePP[0][2]<=objPP[0][2] && cubePP[1][2]>=objPP[1][2] &&
           cubePP[0][0]<=objPP[0][0] && cubePP[1][0]>=objPP[1][0]

        ){
            Log.d("catchCheck 여", "부딪쳤다");
        }

    }
}