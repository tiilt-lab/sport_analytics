package com.example.sensorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Choose_A_Hand extends WearableActivity {

    private TextView mTextView;
    private Button mBtn_Dominant;
    private Button mBtn_NonDominant;
    String activityType;
    String UserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose__a__hand);

        mTextView = (TextView) findViewById(R.id.user_id);
        mBtn_Dominant = (Button) findViewById(R.id.dominant);
        mBtn_NonDominant = (Button) findViewById(R.id.non_dominant);
        activityType =  getIntent().getStringExtra("Label_type");
        UserId = getIntent().getStringExtra("UserId");
        mTextView.setText(UserId);

        mBtn_Dominant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Choose_A_Hand.this , Sensing.class);
                intent.putExtra("Label_type" , activityType);
                intent.putExtra("Hand", "Dominant");
                intent.putExtra("UserId", UserId);
                startActivity(intent);
            }
        });

        mBtn_NonDominant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Choose_A_Hand.this , Sensing.class);
                intent.putExtra("Label_type" , activityType);
                intent.putExtra("Hand", "Non-Dominant");
                intent.putExtra("UserId", UserId);
                startActivity(intent);


            }
        });

        // Enables Always-on
        setAmbientEnabled();

    }
}
