package com.example.sensorapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.util.List;

public class Gyroscope extends WearableActivity {

    private TextView mTextView;
    private TextView mxyz;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener listener;
    String TAG = "WearActivity";
    private List<Sensor> sensors;
    static float x;
    static float y;
    static float z;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private String macAddress;
    private ToggleButton mToggle;
    private JavaGetRequest mTask;
    int on_off = 0;
    String activityType;
    String hand;
    String UserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        mTextView = (TextView) findViewById(R.id.text);
        mxyz = (TextView) findViewById(R.id.xyz);
        activityType =  getIntent().getStringExtra("Label_type");
        hand = getIntent().getStringExtra("Hand");
        UserId = getIntent().getStringExtra("UserId");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() < 1) {
            Toast.makeText(this, "No sensors returned from getSensorList", Toast.LENGTH_SHORT).show();
            Log.wtf(TAG,"No sensors returned from getSensorList");
        }
        Sensor[] sensorArray = sensors.toArray(new Sensor[sensors.size()]);
        for (int i = 0; i < sensorArray.length; i++) {
            Log.wtf(TAG,"Found sensor " + i + " " + sensorArray[i].toString());
        }

        macAddress =
                android.provider.Settings.Secure.getString(this.getApplicationContext().getContentResolver(), "android_id");

        mToggle = (ToggleButton) findViewById(R.id.toggleButton);
        //ToggleButton toggle = (ToggleButton) findViewById(R.id.togglebutton);
        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    on_off = 1;
                } else {
                    // The toggle is disabled
                    //Intent intent=new Intent(Accelerometer.this,Accelerometer.class);
                    //startActivity(intent);
                    //finish();

                    on_off = 0;
                }
            }
        });


        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensor();

    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensor();

    }
    @Override
    protected void onStop() {
        super.onStop();
        //just to make sure.
        unregisterSensor();

    }


    void registerSensor() {
        //just in case
        if (sensorManager == null)
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if(sensors.size() > 0)
            sensor = sensors.get(0);

        listener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);

            }
            @Override
            public void onSensorChanged(SensorEvent event) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                final String[] output = new String[2];
                //just set the values to a textview so they can be displayed.
                if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    int isX = getIntent().getIntExtra("X_is_on", 0);
                    int isY = getIntent().getIntExtra("Y_is_on", 0);
                    int isZ = getIntent().getIntExtra("Z_is_on", 0);

                    if(isX == 1 && isY ==0 && isZ == 0){
                        String my_msg = "Gyroscope" + "\n x: " + String.valueOf(event.values[0]);
                        mxyz.setText(my_msg);
                        float k = event.values[0];
                        String nums = df.format(k);
                        mTextView.setText(nums);

                        String url_value_string = Float.toString(x)+"/"+"0/0";

                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+url_value_string;


                    }

                    else if(isX == 0 && isY ==1 && isZ == 0){
                        String my_msg = "Gyroscope" + "\n y: " + String.valueOf(event.values[1]);
                        mxyz.setText(my_msg);
                        float k = event.values[0];
                        String nums = df.format(k);
                        mTextView.setText(nums);
                        String url_value_string = "0/"+Float.toString(y)+"/0";
                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+url_value_string;


                    }

                    else if(isX == 0 && isY ==0 && isZ == 1){
                        String my_msg = "Gyroscope" + "\n z: " + String.valueOf(event.values[2]);
                        mxyz.setText(my_msg);
                        float k = event.values[2];
                        String nums = df.format(k);
                        mTextView.setText(nums);
                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        String url_value_string = "0/0/"+Float.toString(event.values[2]);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+url_value_string;

                    }
                    else if(isX == 1 && isY ==1 && isZ == 1){
                        String msg = "Gyroscope" + "\n x: " + String.valueOf(event.values[0]) +
                                "\n y: " + String.valueOf(event.values[1]) +
                                "\n z: " + String.valueOf(event.values[2]); //+

                        mxyz.setText(msg);
                        double k = Math.sqrt(((event.values[0] * event.values[0]) + (event.values[1] * event.values[1]) + (event.values[2] * event.values[2])));
                        String nums = Double.toString(k);
                        nums = df.format(k);
                        mTextView.setText(nums);
                        String j = String.valueOf(event.values[0])+"/"+String.valueOf(event.values[1])+"/"+String.valueOf(event.values[2]);
                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+j;

                    }
                    else if(isX == 1 && isY ==1 && isZ == 0){
                        String msg = "Gyroscope" + "\n x: " + String.valueOf(event.values[0]) +
                                "\n y: " + String.valueOf(event.values[1]); //+

                        mxyz.setText(msg);
                        float k = (float) Math.sqrt(((event.values[0] * event.values[0]) + (event.values[1] * event.values[1])));
                        String nums = df.format(k);
                        mTextView.setText(nums);

                        String url_value_string = Float.toString(x)+"/"+Float.toString(y)+"/0";
                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+url_value_string;
                    }
                    else if(isX == 1 && isY ==0 && isZ == 1){
                        String msg = "Gyroscope" + "\n x: " + String.valueOf(event.values[0]) +
                                "\n z: " + String.valueOf(event.values[2]);
                        mxyz.setText(msg);

                        float k = (float) Math.sqrt(((event.values[0] * event.values[0]) + (event.values[2] * event.values[2])));
                        String nums = df.format(k);
                        mTextView.setText(nums);

                        String url_value_string = Float.toString(x)+"/0/"+Float.toString(z);
                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+url_value_string;
                    }
                    else if(isX == 0 && isY ==1 && isZ == 1){
                        String msg = "Gyroscope" + "\n y: " + String.valueOf(event.values[1]) +
                                "\n z: " + String.valueOf(event.values[2]); //+
                        mxyz.setText(msg);

                        float k = (float) Math.sqrt(((event.values[1] * event.values[1]) + (event.values[2] * event.values[2])));
                        String nums = df.format(k);
                        mTextView.setText(nums);

                        String url_value_string = "0/"+Float.toString(y)+"/"+Float.toString(z);
                        long unixTime = System.currentTimeMillis() / 1000L;
                        String time = Long.toString(unixTime);
                        output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Gyroscope/"+url_value_string;
                    }

                    if(on_off ==1) {
                        new JavaGetRequest().execute(output);
                    }


                }
            }
        };
        sensorManager.registerListener(listener, sensor, 1);

    }

    void unregisterSensor() {
        if (sensorManager != null && listener != null) {
            sensorManager.unregisterListener(listener);
        }
        //clean up and release memory.
        sensorManager = null;
        listener = null;


    }



}
