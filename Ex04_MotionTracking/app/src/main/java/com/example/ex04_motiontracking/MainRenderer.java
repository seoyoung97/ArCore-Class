package com.example.ex04_motiontracking;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Session;



import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    interface RenderCallBack{
        void preRender();
    }
    int width, height;
    RenderCallBack myCallBack;
    boolean viewportChange = false;

    CameraPreView mCamera;

    PointCloudRenderer mPointCloud;

    Sphere sphere;

    Line mLineX,mLineY,mLineZ;

    MainRenderer(RenderCallBack myCallBack, Context context){
        this.myCallBack = myCallBack;
        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();

        sphere = new Sphere();
        Log.d("MainRenderer 여","생성자여");

    }
    

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glClearColor(0f,1f,1f,1f);
        mCamera.init();
        mPointCloud.init();
        sphere.init();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES30.glViewport(0,0,width, height);
        viewportChange = true;
        this.width = width;
        this.height = height;


    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        //mainActivity 로부터 카메라 화면 정보를 받기 위해 메소드 실행
        myCallBack.preRender();



        GLES30.glDepthMask(false);
        mCamera.draw();
        GLES30.glDepthMask(true);

        mPointCloud.draw();
        sphere.draw();

        if(mLineX!=null){
            if(!mLineX.isInited){
                mLineX.init();
            }
            mLineX.draw();
        }
        if(mLineY!=null){
            if(!mLineY.isInited){
                mLineY.init();
            }
            mLineY.draw();
        }
        if(mLineZ!=null){
            if(!mLineZ.isInited){
                mLineZ.init();
            }
            mLineZ.draw();
        }
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

    void addPoint(float x, float y, float z){

        float [] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        Matrix.translateM(matrix,0,x,y,z);

        sphere.addNOCnt();
        sphere.setmModelMatrix(matrix);

    }

    void updateProjMatrix(float [] matrix){
        mPointCloud.updateProjMatrix(matrix);
        sphere.updateProjMatrix(matrix);
        if(mLineX!=null){
            mLineX.updateProjMatrix(matrix);
        }
        if(mLineY!=null){
            mLineY.updateProjMatrix(matrix);
        }
        if(mLineZ!=null){
            mLineZ.updateProjMatrix(matrix);
        }
    }

    void updateViewMatrix(float [] matrix){
        mPointCloud.updateViewMatrix(matrix);
        sphere.updateViewMatrix(matrix);
        if(mLineX!=null){
            mLineX.updateViewMatrix(matrix);
        }
        if(mLineY!=null){
            mLineY.updateViewMatrix(matrix);
        }
        if(mLineZ!=null){
            mLineZ.updateViewMatrix(matrix);
        }
    }

    void setmLineX(float [] end,float x, float y, float z){
        mLineX = new Line(end,x,y,z, Color.RED);

        float [] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        Matrix.translateM(matrix,0,x,y,z);

        mLineX.setmModelMatrix(matrix);

    }

    void setmLineY(float [] end,float x, float y, float z){
        mLineY = new Line(end,x,y,z, Color.GREEN);

        float [] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        Matrix.translateM(matrix,0,x,y,z);

        mLineY.setmModelMatrix(matrix);

    }

    void setmLineZ(float [] end,float x, float y, float z){
        mLineZ = new Line(end,x,y,z, Color.BLUE);

        float [] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        Matrix.translateM(matrix,0,x,y,z);

        mLineZ.setmModelMatrix(matrix);

    }

}
