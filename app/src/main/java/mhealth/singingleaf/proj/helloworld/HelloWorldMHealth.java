package mhealth.singingleaf.proj.helloworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

public class HelloWorldMHealth extends AppCompatActivity implements SensorEventListener {

    int status = 0; // application status: 0 off, 1 on
    private long lastUpdate = 0; // used to capture shake
    private float last_x, last_y, last_z; // used to capture shake
    private static final int SHAKE_THRESHOLD = 600; // speed threshold above which a motion can be judged as a shake
    Double threshold = 1200.0; // speed threshold above which send a "Hello" to server, otherwise send a "World"
    TextView txtThreshold;
    Button bntUpdate;
    ServerCommunicator sc = new ServerCommunicator();
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world_mhealth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // get access to accelerometer sensor
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // start and end button
        final FloatingActionButton fab_s = (FloatingActionButton) findViewById(R.id.fab_s);
        final FloatingActionButton fab_e = (FloatingActionButton) findViewById(R.id.fab_e);
        fab_e.hide();
        fab_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == 0) {
                    Snackbar.make(view, "Starting...", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    // register sensor listener
                    senSensorManager.registerListener(
                            HelloWorldMHealth.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    // change app status
                    status = 1;
                    fab_s.hide();
                    fab_e.show();
                }
            }
        });
        fab_e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == 1) {
                    Snackbar.make(view, "Ending...", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    // unregister sensor listener
                    senSensorManager.unregisterListener(HelloWorldMHealth.this);
                    // change app status
                    status = 0;
                    fab_e.hide();
                    fab_s.show();
                }
            }
        });

        // 1. Access the TextView defined in layout XML
        // and then set its text
        txtThreshold = (TextView) findViewById(R.id.main_textview);
        txtThreshold.setText(threshold.toString());

        // 2. Access the Button defined in layout XML
        // and listen for it here
        bntUpdate = (Button) findViewById(R.id.main_button);

        bntUpdate.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v) {
                        displayDialog();
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hello_world_mhealth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void displayDialog() {

        // show a dialog to ask for threshold
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Threshold Setting");
        alert.setMessage("Please input the threshold");

        // Create EditText for entry
        final EditText input = new EditText(this);
        alert.setView(input);

        // Make an "OK" button to save the name
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                // Grab the EditText's input
                threshold = Double.parseDouble(input.getText().toString());
                txtThreshold.setText(threshold.toString());
            }
        });

        // Make a "Cancel" button
        // that simply dismisses the alert
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
        }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                // calculate shaking speed
                double speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
                // post to server
                if (speed > SHAKE_THRESHOLD) {
                    RequestParams params = new RequestParams();
                    params.put("content", ((Double) speed).toString());
                    params.put("threshold", threshold.toString());
                    HelloWorldMHealth.this.sc.postToServer(params);

                    /**
                    if (HelloWorldMHealth.this.sc.postToServer(params)) {
                        Toast.makeText(getApplicationContext(),
                                "Sent to Server!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Failed to send to Server!",
                                Toast.LENGTH_SHORT).show();
                    }
                     */
                    Toast.makeText(getApplicationContext(),
                            "Action detected!",
                            Toast.LENGTH_SHORT).show();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }



}
