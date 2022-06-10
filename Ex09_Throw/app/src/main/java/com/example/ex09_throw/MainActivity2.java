package com.example.ex09_throw;

import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageEvents;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void EventBtn2 (View view) {
        Toast.makeText(this, "게임을 다시시작합니다", Toast.LENGTH_SHORT).show();
        finish();
    }
}