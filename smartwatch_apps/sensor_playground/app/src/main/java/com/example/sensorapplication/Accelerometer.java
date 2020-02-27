package com.example.sensorapplication;

import android.app.DownloadManager;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Accelerometer extends WearableActivity {

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
        setContentView(R.layout.activity_accelerometer);

        mTextView = (TextView) findViewById(R.id.text);
        mxyz = (TextView) findViewById(R.id.xyz);
        activityType =  getIntent().getStringExtra("Label_type");
        hand =  getIntent().getStringExtra("Hand");
        UserId = getIntent().getStringExtra("UserId");

        Log.d("Activity Type",activityType);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() < 1) {
            Toast.makeText(this, "No sensors returned from getSensorList", Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "No sensors returned from getSensorList");
        }
        Sensor[] sensorArray = sensors.toArray(new Sensor[sensors.size()]);
        for (int i = 0; i < sensorArray.length; i++) {
            Log.wtf(TAG, "Found sensor " + i + " " + sensorArray[i].toString());
        }
        //mTask = new JavaGetRequest();
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
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        if (sensors.size() > 0)
            sensor = sensors.get(0);

        listener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);

            }

            // code goes here
            @Override
            public void onSensorChanged(SensorEvent event) {
                // just set the values to a textview so they can be displayed.
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                final String[] output = new String[2];

                float k = 0;
                String nums;
                String url_value_string = "";
                String msg = "";

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                    int isX = getIntent().getIntExtra("X_is_on", 0);
                    int isY = getIntent().getIntExtra("Y_is_on", 0);
                    int isZ = getIntent().getIntExtra("Z_is_on", 0);

                    if(isX == 1 && isY == 0 && isZ == 0){
                        msg = "Acc" + "\n x: " + String.valueOf(event.values[0]);
                        k = event.values[0];
                        url_value_string = Float.toString(x)+"/"+"0/0";
                    }

                    else if(isX == 0 && isY ==1 && isZ == 0){
                        msg = "Acc" + "\n y: " + String.valueOf(event.values[1]);
                        k = event.values[0];
                        url_value_string = "0/"+Float.toString(y)+"/0";
                    }

                    else if(isX == 0 && isY ==0 && isZ == 1){
                        msg = "Acc" + "\n z: " + String.valueOf(event.values[2]);
                        k = event.values[2];
                        url_value_string = "0/0/"+Float.toString(event.values[2]);
                    }
                    else if(isX == 1 && isY ==1 && isZ == 1){
                        msg = "Acc" + "\n x: " + String.valueOf(event.values[0]) +
                                "\n y: " + String.valueOf(event.values[1]) +
                                "\n z: " + String.valueOf(event.values[2]);

                        k = (float) Math.sqrt(((event.values[0] * event.values[0]) + (event.values[1] * event.values[1]) + (event.values[2] * event.values[2])));
                        url_value_string = String.valueOf(event.values[0])+"/"+String.valueOf(event.values[1])+"/"+String.valueOf(event.values[2]);
                    }
                    else if(isX == 1 && isY ==1 && isZ == 0){
                        msg = "Acc" + "\n x: " + String.valueOf(event.values[0]) +
                                "\n y: " + String.valueOf(event.values[1]);

                        k = (float) Math.sqrt(((event.values[0] * event.values[0]) + (event.values[1] * event.values[1])));
                        url_value_string = Float.toString(x)+"/"+Float.toString(y)+"/0";
                    }
                    else if(isX == 1 && isY ==0 && isZ == 1){
                        msg = "Acc" + "\n x: " + String.valueOf(event.values[0]) +
                                "\n z: " + String.valueOf(event.values[2]);

                        k = (float) Math.sqrt(((event.values[0] * event.values[0]) + (event.values[2] * event.values[2])));
                        url_value_string = Float.toString(x)+"/0/"+Float.toString(z);
                    }
                    else if(isX == 0 && isY ==1 && isZ == 1){
                        msg = "Acc" + "\n y: " + String.valueOf(event.values[1]) +
                                "\n z: " + String.valueOf(event.values[2]);

                        k = (float) Math.sqrt(((event.values[1] * event.values[1]) + (event.values[2] * event.values[2])));
                        url_value_string = "0/"+Float.toString(y)+"/"+Float.toString(z);
                    }

                    mxyz.setText(msg);
                    nums = df.format(k);
                    mTextView.setText(nums);
                    long unixTime = System.currentTimeMillis() / 1000L;
                    String time = Long.toString(unixTime);

                    // Lab Server
                    output[0] = "http://165.124.181.163:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Acc/"+url_value_string;

                    // Dev Server
                    // output[0] = "http://192.168.43.192:5000/store/"+UserId+"/"+activityType+"-"+hand+"/"+time+"/Acc/"+url_value_string;


                    //new CallAPI().execute(output);
                    //new JavaPostRequest().execute(output);
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
        // clean up and release memory.
        sensorManager = null;
        listener = null;


    }



    //  class BackgroundTask extends AsyncTask<String, Void, Void >{

    //     Socket s;
    //   PrintWriter printWriter;

    //    @Override
    //    protected Void doInBackground(String... voids) {

    //      try {
    //        String data = voids[0];
    //      s = new Socket();
    //      printWriter = new PrintWriter(s.getOutputStream());
    //            printWriter.write(data);
    //          printWriter.flush();
    //        printWriter.close();
    //      s.close();
    //          }catch (IOException e){
    //            e.printStackTrace();
    //      }
//
    //          return null;
    //    }
    //  }

   class CallAPI extends AsyncTask<String, Void, String> {

        public CallAPI() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // URL to call
            String data = params[0]; //data to post
            String my_url = params[1];
            OutputStream out = null;
            String output = "";
            int RC = 0;



            try {
                URL url = new URL(my_url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestMethod("GET");
                urlConnection.setRequestMethod("POST");
                out = new BufferedOutputStream(urlConnection.getOutputStream());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                RC = responseCode;
                //System.out.println("\nSending 'POST' request to URL : " + url);
                //System.out.println("Post parameters : " + urlParameters);
                //System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                output = response.toString();

            } catch (Exception e) {
                Log.d("Wear Activity", "doInBackground: Exception: "+e.toString());
                Log.d("Wear Activity", "doInBackground: Exception: "+ Integer.toString(RC));
            }




            return output;

        }

        protected void onPostExecute(String feed) {
            Log.d("Wear Activity", "onPostExecute: Result:  " + feed);
        }


    }



/*    private void Submit(String data)
    {
        final String savedata= data;
        String URL="https://YOUR_API_URL";
        RequestQueue requestQueue;

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject objres=new JSONObject(response);
                    Toast.makeText(getApplicationContext(),objres.toString(),Toast.LENGTH_LONG).show();


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_LONG).show();

                }
                //Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                //Log.v("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding while trying to get the bytes", data);
                    return null;
                }
            }

        };
        requestQueue.add(stringRequest);
    }*/
}




