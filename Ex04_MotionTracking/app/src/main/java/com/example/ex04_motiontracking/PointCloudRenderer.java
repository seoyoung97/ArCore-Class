package com.example.ex04_motiontracking;

import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.PointCloud;

import java.util.HashMap;


public class PointCloudRenderer {

    float [] mViewMatrix = new float[16];
    float [] mProjMatrix = new float[16];

    //GPU  점 위치 연산 함수
    String vertexShaderString =
            "uniform mat4 uMVPMatrix; \n"+ //모델 * 뷰 * 프로젝션의 위치 매트릭스(4 * 4)
                    "uniform float uPointSize; \n" + // 색상
                    "uniform vec4 uColor; \n" + // 색상
                    "varying vec4 vColor; \n" + // 색상

                    "attribute vec4 aPosition; \n" + // vec4 --> 크기4, 점들
                    "void main(){ \n"+
                    "vColor = uColor ; \n"+
                    "gl_Position = uMVPMatrix * vec4( aPosition.xyz , 1.0) ; \n"+

                    "gl_PointSize = uPointSize ; \n"+
                    "}";

    //GPU  점 색상 연산 함수
    String fragmentShaderString =
            "precision mediump float; \n"+ //해상도를 중간으로
                    "varying vec4 vColor; \n" + // 색상
                    "void main(){ \n"+
                    "gl_FragColor = vColor ; \n"+
                    "}";



    int [] mVbo;

    HashMap<String, float[]> ccs = new HashMap<>();

    //색상값   -- 주황색 ==> 배열로도 가능
    float [] color;


    PointCloudRenderer(){
        ccs.put("빨강",new float[]{1.0f, 0f,0f, 1.0f});
        ccs.put("노랑",new float[]{1.0f, 1.0f,0f, 1.0f});
        ccs.put("초록",new float[]{ 0f,1.0f,0f, 1.0f});
        ccs.put("하늘",new float[]{ 0f,1.0f,1.0f, 1.0f});

        color = ccs.get("빨강");
    }



    float pointSize = 50f;

    int mNumPoints = 0;

    //GPU 연산식 번호
    int mProgram;

    //생성자
    void init(){

        mVbo = new int[1];

        GLES30.glGenBuffers(1, mVbo,0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,1000*16,null, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        //점 위치 계산식 변수 번호 지정
        int vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        GLES30.glShaderSource(vertexShader, vertexShaderString);
        GLES30.glCompileShader(vertexShader);


        //점 색상 계산식 변수 번호 지정
        int fragmentShader  = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragmentShader , fragmentShaderString);
        GLES30.glCompileShader(fragmentShader );


        mProgram = GLES30.glCreateProgram();

       GLES30.glAttachShader(mProgram, vertexShader);
       GLES30.glAttachShader(mProgram, fragmentShader);

        GLES30.glLinkProgram(mProgram); //GPU  계산 번호를 링크를 건다 --> 그릴때 링크된 번호를 불러와 연산한다.
        Log.d("PointCloudRenderer 여","init() ");
    }

    void draw(){

        float [] mMVPMatrix = new float[16];

        Matrix.multiplyMM(mMVPMatrix,0,mProjMatrix,0,mViewMatrix,0);

        GLES30.glUseProgram(mProgram); //링크 건 계산 식을 가져온다.

        //점 정보 변수
        int aPosition = GLES30.glGetAttribLocation(mProgram, "aPosition");
        //색 정보 변수 번호
        int uColor = GLES30.glGetUniformLocation(mProgram, "uColor");
        //메트릭스 정보 변수 변호
        int uMVPMatrix =  GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        //색 정보 변수 번호
        int uPointSize = GLES30.glGetUniformLocation(mProgram, "uPointSize");

        GLES30.glEnableVertexAttribArray(aPosition); //aPosition 변수번호 사용하는 정보를 배열 형태로 사용한다.

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVbo[0]);


       // Log.d("PointCloudRenderer 여","draw() ");
        //점 정보 넣기
        GLES30.glVertexAttribPointer(
                aPosition, //배열을 넣을 계산식 변수번호
                4,      //점의 좌표계 갯수
                GLES30.GL_FLOAT,  //점 자료형 float
                false,   //점 정규화 하지 않음
                4 * 4 ,  // 점 한개에 대한 정보 크기 (x,y,z)* float
                0
        );

        //점크기 넣기
        GLES30.glUniform1f(uPointSize, pointSize);

        //메트릭스 정보 넣기
        GLES30.glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        //색정보
        //색정보 넣기
        GLES30.glUniform4fv(uColor, 1, color, 0);
       // GLES30.glUniform4f(uColor, 1f, 0f, 0f, 1f);

        //그리는 순서에 따라 그린다.
        GLES30.glDrawArrays(
                GLES30.GL_POINTS,
               0,
                mNumPoints
        );

        //그리기 닫는다.
        GLES30.glDisableVertexAttribArray(aPosition);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


    }

    void updateProjMatrix(float [] matrix){
        System.arraycopy(matrix, 0, mProjMatrix,0,16);
    }

    void updateViewMatrix(float [] matrix){
        System.arraycopy(matrix, 0, mViewMatrix,0,16);
    }

    void update(PointCloud pointCloud){
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVbo[0]);

        mNumPoints = pointCloud.getPoints().remaining() / 4;
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER,0,mNumPoints * 16, pointCloud.getPoints());

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

       // Log.d("PointCloudRenderer 여","update() ");
    }

    void setColor(int cc){
        Log.d("PointCloudRenderer 여",""+cc);

        color = new float[]{
                Color.red(cc)/255f,
                Color.green(cc)/255f,
                Color.blue(cc)/255f,
                 1.0f};
    }

    void setColor(String title){
        Log.d("PointCloudRenderer 여",""+title);

        color = ccs.get(title);
    }

}
