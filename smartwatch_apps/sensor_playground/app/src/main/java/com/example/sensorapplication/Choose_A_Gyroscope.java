package com.example.sensorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Choose_A_Gyroscope extends WearableActivity {

    private ToggleButton xBtn;
    private ToggleButton yBtn;
    private ToggleButton zBtn;
    private Button magBtn;
    int x_isOn = 0;
    int y_isOn = 0;
    int z_isOn = 0;
    String activityType;
    String hand;
    String UserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose__a__gyroscope);

        xBtn = (ToggleButton) findViewById(R.id.xBtn);
        yBtn = (ToggleButton) findViewById(R.id.yBtn);
        zBtn = (ToggleButton) findViewById(R.id.zBtn);
        magBtn = (Button) findViewById(R.id.magBtn);
        activityType =  getIntent().getStringExtra("Label_type");
        hand =  getIntent().getStringExtra("Hand");
        UserId = getIntent().getStringExtra("UserId");

/*        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Choose_A_Accelerometer.this , AccelerometerX.class));
            }

        });

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Choose_A_Accelerometer.this , AccelerometerY.class));
            }

        });

        zBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Choose_A_Accelerometer.this , AccelerometerZ.class));
            }

        });*/

        xBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    x_isOn = 1;
                } else {
                    // The toggle is disabled
                    //Intent intent=new Intent(Accelerometer.this,Accelerometer.class);
                    //startActivity(intent);
                    //finish();

                    x_isOn = 0;
                }
            }
        });

        yBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    y_isOn = 1;
                } else {
                    // The toggle is disabled
                    //Intent intent=new Intent(Accelerometer.this,Accelerometer.class);
                    //startActivity(intent);
                    //finish();

                    y_isOn = 0;
                }
            }
        });

        zBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    z_isOn = 1;
                } else {
                    // The toggle is disabled
                    //Intent intent=new Intent(Accelerometer.this,Accelerometer.class);
                    //startActivity(intent);
                    //finish();

                    z_isOn = 0;
                }
            }
        });




        magBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Choose_A_Gyroscope.this, Gyroscope.class);
                intent.putExtra("X_is_on", x_isOn);
                intent.putExtra("Y_is_on", y_isOn);
                intent.putExtra("Z_is_on", z_isOn);
                intent.putExtra("Label_type", activityType);
                intent.putExtra("UserId", UserId);
                startActivity(intent);
            }

        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
