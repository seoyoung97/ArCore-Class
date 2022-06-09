package com.example.ex01_opengl;



import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    ObjRenderer obj;

    //Square myBox;
    ArrayList<Square> boxs;

    float [] mMVPMatrix = new float[16];
    float [] mViewMatrix = new float[16];
    float [] mProjectionMatrix = new float[16];
    float [][] pos = {
            {
                    -0.5f, 0.5f, 0f,    //왼쪽 위
                    -0.5f, -0.5f,0f,    //왼쪽 아래
                    0.5f,  -0.5f,0f,     //오른쪽 아래
                    0.5f,  0.5f,0f     //오른쪽  위
            },
            {
                    -0.5f, 0.5f, -1f,
                    -0.5f, 0.5f, 0f,
                    0.5f,  0.5f,0f,
                    0.5f,  0.5f,-1f
            },
            {
                    0.5f,  0.5f,0f,
                    0.5f,  -0.5f,0f,
                    0.5f,  -0.5f,-1f,
                    0.5f,  0.5f,-1f
            }
    };

    float [][] ccs = {
            {0.0f, 0.5f,1.0f, 1.0f},
            {0.0f, 1f,0.0f, 1.0f},
            {1f, 0f,0.0f, 1.0f},
    };


    MainRenderer(Context context){
        obj = new ObjRenderer(context, "andy.obj", "andy.png");
    }




    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glClearColor(1.0f,1.0f,0f,1f);
       /* myBox = new Square(new float[]{

                //x,     y,  z
                -0.5f, 0.5f, 0f,    //왼쪽 위
                -0.5f, -0.5f,0f,    //왼쪽 아래
                0.5f,  -0.5f,0f,     //오른쪽 아래
                0.5f,  0.5f,0f     //오른쪽  위
        }, new float[] {0.0f, 0.5f,1.0f, 1.0f}
                );*/

        boxs = new ArrayList<>();
        for (int i = 0; i <pos.length ; i++) {
            boxs.add(new Square(pos[i], ccs[i]));
        }
        obj.init();

        Log.d("MainRenderer 여","onSurfaceCreated");

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES30.glViewport(0,0,width,height);

        float ratio = (float)width*1/height;

        //프로젝션 메트릭스
        Matrix.frustumM(mProjectionMatrix,0,
                -ratio,ratio,-0.9f,1.0f,
                5,300);

        Log.d("MainRenderer 여","onSurfaceChanged");
    }

    @Override
    public void onDrawFrame(GL10 gl10) {


        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        //카메라 메트릭스
        Matrix.setLookAtM(
                mViewMatrix,0,
                //카메라위치
                // x,      y,      z
                2,2,20,
                //시선 위치
                // x,      y,      z
                0,1,-2,
                //카메라 윗방향
                0,1,0
        );


        //메트릭스 곱하기
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
        //박스 그리기
       // myBox.draw(mMVPMatrix);

        for (Square box : boxs) {
           // box.draw(mMVPMatrix);
        }

        float [] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix,0);
        obj.setModelMatrix(modelMatrix);
        obj.setViewMatrix(mViewMatrix);
        obj.setProjectionMatrix(mProjectionMatrix);

        obj.draw();


        Log.d("MainRenderer 여","onDrawFrame");
    }

    static int loadShader(int type, String shaderCode){
        int res = GLES30.glCreateShader(type);
        GLES30.glShaderSource(res, shaderCode);
        GLES30.glCompileShader(res);
        return res;
    }
}
