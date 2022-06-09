package com.example.ex01_opengl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    MyGLSurfaceView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //OpenGL 그려질 객체(화면) 생성
        myView = new MyGLSurfaceView(this);

        //main.xml 화면에 붙이기
        setContentView(myView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Main 여","onPause!!!");
        myView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main 여","onResume!!!");
        myView.onResume();
    }

}