package com.example.sensorapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class Get_User_ID extends WearableActivity {

    private TextView mTextView;
    public static TextView mUserId;
    public static Boolean isClicked;
    private Button getUserId;
    private Button next_page;
    public static String myId;
    private JavaGetRequest myTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__user__id);

        mTextView = (TextView) findViewById(R.id.text);
        getUserId = (Button) findViewById(R.id.get_id);
        next_page = (Button) findViewById(R.id.next);
        mUserId = (TextView) findViewById(R.id.user_id);
        isClicked = false;
        mUserId.setText("USER ID");
        myId = "";

        getUserId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String output = "http://165.124.181.163:5000/new_id";
                isClicked = true;
                new JavaGetRequest().execute(output);

            }
        });

        next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Get_User_ID.this , Choose_A_Label.class);
                intent.putExtra("UserId", myId);
                startActivity(intent);


            }
        });

        // Enables Always-on
        setAmbientEnabled();



    }
}
