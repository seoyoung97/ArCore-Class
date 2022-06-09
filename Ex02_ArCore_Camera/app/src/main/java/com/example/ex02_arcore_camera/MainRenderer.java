package com.example.ex02_arcore_camera;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Session;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    interface RenderCallBack{
        void preRender();
    }
    int width, height;
    RenderCallBack myCallBack;
    boolean viewportChange = false;

    CameraPreView mCamera;

    ArrayList<ObjRenderer>  objs;
    int cnt = 0;


    float [] mViewMatrix = new float[16];
    float [] mProjectionMatrix = new float[16];

    
    MainRenderer(RenderCallBack myCallBack, Context context){
        this.myCallBack = myCallBack;
        mCamera = new CameraPreView();
        objs = new ArrayList<>();
        //objs.add(new ObjRenderer(context, "bed.obj","bed.jpg"));
        objs.add(new ObjRenderer(context, "crate.obj","Crate_Base_Color.png"));
        objs.add(new ObjRenderer(context, "chair.obj","chair.jpg"));
        objs.add(new ObjRenderer(context, "table.obj","table.jpg"));
        objs.add(new ObjRenderer(context, "andy.obj","andy.png"));


        Log.d("MainRenderer 여","생성자여");

    }
    

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0f,1f,1f,1f);
        mCamera.init();
        for (ObjRenderer obj :  objs) {
            obj.init();
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewportChange = true;
        this.width = width;
        this.height = height;

        float ratio = (float)width*10/height;

        //프로젝션 메트릭스
        Matrix.frustumM(mProjectionMatrix,0,
                -ratio,ratio,-10f,10f,
                20,300);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        //mainActivity 로부터 카메라 화면 정보를 받기 위해 메소드 실행
        myCallBack.preRender();

        //카메라 메트릭스
        Matrix.setLookAtM(
                mViewMatrix,0,
                //카메라위치
                // x,      y,      z
                2,5,100,
                //시선 위치
                // x,      y,      z
                0,0,-2,
                //카메라 윗방향
                0,1,0
        );

        GLES30.glDepthMask(false);
        mCamera.draw();
        GLES30.glDepthMask(true);

        float [] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix,0);

        ObjRenderer obj = objs.get(cnt);

        obj.setModelMatrix(modelMatrix);
        obj.setViewMatrix(mViewMatrix);
        obj.setProjectionMatrix(mProjectionMatrix);

        obj.draw();
    }

    //화면 변화 감지하면 내가 실행된다.
    void onDisplayChanged(){
        viewportChange = true;
        Log.d("MainRenderer 여","onDisplayChanged 여");
    }


    int getTextureID(){  //카메라의 색칠하기 id를 리턴 한다.
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }

    void updateSession(Session session, int rotation){
        if(viewportChange){
            session.setDisplayGeometry(rotation, width, height);


            viewportChange = false;
        }
    }
}
