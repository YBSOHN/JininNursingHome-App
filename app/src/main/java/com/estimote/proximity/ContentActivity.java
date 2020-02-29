package com.estimote.proximity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

public class ContentActivity extends AppCompatActivity {

    TextView et_location, et_head, et_body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_content);


        Intent intent = getIntent();
        et_location = findViewById(R.id.et_location);
        et_head = findViewById(R.id.et_head);
        et_body = findViewById(R.id.et_body);
        et_location.setText(intent.getStringExtra("name"));
        et_head.setText(intent.getStringExtra("subject"));
        et_body.setText(intent.getStringExtra("info"));
    }
}
