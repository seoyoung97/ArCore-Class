package com.example.ex02_arcore_camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ZoomButtonsController;
import android.widget.ZoomControls;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    Session mSession;

    GLSurfaceView mySurfaceView;

    TextView myTextView;

    MainRenderer mRenderer;
    
    HashMap<String, Integer> map;

    ZoomControls zoomControls;






        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hidStatusBarTitleBar();
        setContentView(R.layout.activity_main);

        mySurfaceView = (GLSurfaceView)findViewById(R.id.glSsurfaceview);
        myTextView = (TextView)findViewById(R.id.myTextView);

        map = new HashMap<>();
        map.put("박스",0);
        map.put("의자",1);
        map.put("책상",2);
        map.put("안디",3);

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
    
    public void onBtnClk(View view){
        Button btn = (Button)view;
        String ttt = btn.getText().toString();

       ColorDrawable cd =  (ColorDrawable)btn.getBackground();
        int cc = cd.getColor();



        mRenderer.cnt = map.get(ttt);
        myTextView.setText(ttt);
        myTextView.setTextColor(cc);
        //Log.d("버튼이여", mRenderer.cnt+"");
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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()  & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//                CameraPreview.mCamera.autoFocus(new Camera.AutoFocusCallback(){
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
//                        return;
//                    }
//                });
//                break;
//            case MotionEvent.ACTION_MOVE: // 터치 후 이동 시
//                if(event.getPointerCount()==2) {
//                    double now_interval_X = (double) abs(event.getX(0) - event.getX(1));
//
//                }
//
//
//        }
    }
