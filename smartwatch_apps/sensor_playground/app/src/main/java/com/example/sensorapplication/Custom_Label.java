package com.example.sensorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class Custom_Label extends WearableActivity {

    private TextView mTextView;
    private TextView mUserId;
    private Button edit;
    private Button confirm;
    String spokenText;
    String activityType;
    String UserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom__label);

        UserId =  getIntent().getStringExtra("UserId");
        mUserId = (TextView) findViewById(R.id.user_id);
        mUserId.setText(UserId);
        mTextView = (TextView) findViewById(R.id.custom_label);
        mTextView.setText(UserId);
        edit = (Button) findViewById(R.id.edit_label);
        confirm = (Button) findViewById(R.id.confirm_label);
        activityType =  getIntent().getStringExtra("Label_type");

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySpeechRecognizer();
                mTextView.setText(spokenText);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Custom_Label.this , Choose_A_Hand.class);
                intent.putExtra("Label_type" , spokenText);
                intent.putExtra("UserId" , UserId);
                startActivity(intent);
            }
        });

        // Enables Always-on
        setAmbientEnabled();

    }

    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            spokenText = results.get(0);
            mTextView.setText(spokenText);
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
