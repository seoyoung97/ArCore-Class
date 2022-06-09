package com.example.ex01_opengl;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    //GPU  점 위치 연산 함수
    String vertexShaderString =
            "uniform mat4 uMVPMatrix; \n"+ //모델 * 뷰 * 프로젝션의 위치 매트릭스(4 * 4)
            "attribute vec4 aPosition; \n" + // vec4 --> 크기4, 점들
            "void main(){ \n"+
             "gl_Position = uMVPMatrix * aPosition ; \n"+
                    "}";

    //GPU  점 색상 연산 함수
    String fragmentShaderString =
            "precision mediump float; \n"+ //해상도를 중간으로
                    "uniform vec4 vColor; \n" + // 색상
                    "void main(){ \n"+
                    "gl_FragColor = vColor ; \n"+
                    "}";


    //점좌표
    float [] coods={

            //x,     y,  z
            -0.5f, 0.5f, 0f,    //왼쪽 위
            -0.5f, -0.5f,0f,    //왼쪽 아래
            0.5f,  -0.5f,0f,     //오른쪽 아래
            0.5f,  0.5f,0f     //오른쪽  위
    };

    //점 그리기 순서
    short [] drawOrder = {
            0,1,2,
            0,2,3
    };

    //색상값   -- 주황색?
    float [] color = {1.0f, 0.5f,0.2f, 1.0f};


    //GPU 계산에 넘길 버퍼
    FloatBuffer vertexBuffer;
    ShortBuffer drawBuffer;

    //GPU 연산식 번호
    int mProgram;

    //생성자
    Square(float [] coods, float [] color){
        this.coods = coods;
        this.color = color;
        //9(점갯수)  * 4(자료형 크기=> float)
        ByteBuffer bb = ByteBuffer.allocateDirect(coods.length *4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer(); //크기 지정
        vertexBuffer.put(coods);      //점버퍼에 점 정보 넣기
        vertexBuffer.position(0); //읽기 위치 0으로 지정


        //점순서 정보
        bb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        bb.order(ByteOrder.nativeOrder());

        drawBuffer = bb.asShortBuffer(); //크기 지정
        drawBuffer.put(drawOrder);      //순서버퍼에 점 정보 넣기
        drawBuffer.position(0); //읽기 위치 0으로 지정

        //점 위치 계산식 변수 번호 지정
        int vertexShader = MainRenderer.loadShader(
                GLES30.GL_VERTEX_SHADER
                ,vertexShaderString);

        //점 색상 계산식 변수 번호 지정
        int fragmentShader = MainRenderer.loadShader(
                GLES30.GL_FRAGMENT_SHADER
                ,fragmentShaderString);


        mProgram = GLES30.glCreateProgram();

        GLES30.glAttachShader(mProgram, vertexShader);
        GLES30.glAttachShader(mProgram, fragmentShader);

        GLES30.glLinkProgram(mProgram); //GPU  계산 번호를 링크를 건다 --> 그릴때 링크된 번호를 불러와 연산한다.
    }

    void draw(float [] mMVPMatrix){
        GLES30.glUseProgram(mProgram); //링크 건 계산 식을 가져온다.

        //점 정보 변수
        int aPosition = GLES30.glGetAttribLocation(mProgram, "aPosition");

        GLES30.glEnableVertexAttribArray(aPosition); //aPosition 변수번호 사용하는 정보를 배열 형태로 사용한다.

        //점 정보 넣기
        GLES30.glVertexAttribPointer(
                aPosition, //배열을 넣을 계산식 변수번호
                3,      //점의 좌표계 갯수
                GLES30.GL_FLOAT,  //점 자료형 float
                false,   //점 정규화 하지 않음
                3 * 4 ,  // 점 한개에 대한 정보 크기 (x,y,z)* float
                vertexBuffer  //실제 점 정보 버퍼
        );

        //색 정보 변수 번호
        int vColor = GLES30.glGetUniformLocation(mProgram, "vColor");

        //색정보 넣기
        GLES30.glUniform4fv(vColor, 1, color, 0);

        //메트릭스 정보 변수 변호
        int uMVPMatrix =  GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        //메트릭스 정보 넣기
        GLES30.glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        //그리는 순서에 따라 그린다.
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES,
                drawOrder.length,
                GLES30.GL_UNSIGNED_SHORT,
                drawBuffer
                );

        //그리기 닫는다.
        GLES30.glDisableVertexAttribArray(aPosition);
    }

}
