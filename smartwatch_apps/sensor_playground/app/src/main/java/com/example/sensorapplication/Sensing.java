package com.example.sensorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class Sensing extends WearableActivity {

    private TextView mTextView;
    private Button mBtn_acl;
    private Button mBtn_gyro;
    private Button mBtn_step;
    private Button mBtn_heart;
    private String activityType;
    private String hand;
    String UserId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensing);

        mTextView = (TextView) findViewById(R.id.user_id);
        mBtn_acl = (Button) findViewById(R.id.accelerometerBtn);
        mBtn_gyro = (Button) findViewById(R.id.gyroscopeBtn);
        mBtn_step = (Button) findViewById(R.id.stepDetectorBtn);
        mBtn_heart = (Button) findViewById(R.id.hearRateBtn);
        activityType =  getIntent().getStringExtra("Label_type");
        hand = getIntent().getStringExtra("Hand");
        UserId = getIntent().getStringExtra("UserId");
        mTextView.setText(UserId);

        mBtn_acl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sensing.this , Choose_A_Accelerometer.class);
                intent.putExtra("Label_type", activityType);
                intent.putExtra("Hand", hand);
                intent.putExtra("UserId", UserId);
                startActivity(intent);
            }
        });

        mBtn_gyro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Sensing.this , Choose_A_Gyroscope.class);
                intent.putExtra("Label_type", activityType);
                intent.putExtra("Hand", hand);
                intent.putExtra("UserId", UserId);
                startActivity(intent);

            }
        });

        mBtn_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sensing.this , HeartRate.class);
                intent.putExtra("Label_type", activityType);
                intent.putExtra("Hand", hand);
                intent.putExtra("UserId", UserId);
                startActivity(intent);
            }
        });

        mBtn_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sensing.this , StepCounter.class);
                intent.putExtra("Label_type", activityType);
                intent.putExtra("Hand", hand);
                intent.putExtra("UserId", UserId);
                startActivity(intent);
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
