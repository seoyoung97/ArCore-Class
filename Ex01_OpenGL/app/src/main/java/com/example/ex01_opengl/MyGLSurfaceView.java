package com.example.ex01_opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class MyGLSurfaceView extends GLSurfaceView {

    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        Log.d("MyGLSurfaceView 여","생성자다!!!");
        //화면 그려줘
        setRenderer(new MainRenderer(context));

        //그리기 데이터 변경시에만 렌더링
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


    }
}
