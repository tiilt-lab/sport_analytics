package com.example.sensorapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Choose_A_Label extends WearableActivity {

    private TextView mTextView;
    private Button mBtn_dribbling;
    private Button mBtn_shooting;
    private Button mBtn_other;
    private Button mBtn_custom;
    private String UserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose__a__label);

        UserId =  getIntent().getStringExtra("UserId");
        mTextView = (TextView) findViewById(R.id.user_id);
        mTextView.setText(UserId);
        mBtn_dribbling = (Button) findViewById(R.id.dribbling);
        mBtn_shooting = (Button) findViewById(R.id.shooting);
        mBtn_other = (Button) findViewById(R.id.other);
        mBtn_custom = (Button) findViewById(R.id.customize);


        mBtn_dribbling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Choose_A_Label.this , Choose_A_Hand.class);
                intent.putExtra("Label_type" , "Dribbling");
                intent.putExtra("UserId" , UserId);
                startActivity(intent);
            }
        });

        mBtn_shooting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Choose_A_Label.this , Choose_A_Hand.class);
                intent.putExtra("Label_type" , "Shooting");
                intent.putExtra("UserId" , UserId);
                startActivity(intent);


            }
        });

        mBtn_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Choose_A_Label.this , Choose_A_Hand.class);
                intent.putExtra("Label_type" , "Other");
                intent.putExtra("UserId" , UserId);
                startActivity(intent);
            }
        });

        mBtn_custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Choose_A_Label.this , Custom_Label.class);
                intent.putExtra("UserId" , UserId);
                startActivity(intent);
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
