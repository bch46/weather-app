package info4120.whatevertheweather;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SensorManager sensorManager;
    private TextView sensorsTot, sensorsAvailable, cellSignal;
    private EditText filenameInput;
    private String logFilename;
    private Button toggleSensors, logInput;
    private boolean sensorsShowing, currentlyLogging;

    /**
     * First method to be called in the Android activity lifecycle. Basically does all of the work
     * of initializing the app so far.
     * More on the activity lifecycle:
     * http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the texts fields of the layout and setup to invisible
        sensorsTot = (TextView) findViewById(R.id.sensor_total);
        sensorsAvailable = (TextView) findViewById(R.id.sensor_avail);
        cellSignal = (TextView) findViewById(R.id.cell_signal);
        filenameInput = (EditText) findViewById(R.id.log_name);

        sensorsAvailable.setVisibility(View.GONE);
        sensorsShowing = false;

        toggleSensors = (Button) findViewById(R.id.show_sensors);
        toggleSensors.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toggleSensorList();
            }
        });

        currentlyLogging = false;
        logInput = (Button) findViewById(R.id.start_logging);
        logInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toggleLogging();
            }
        });

        // Get the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // List of Sensors Available
        List<Sensor> msensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // Print how may Sensors are there
        sensorsTot.setText(msensorList.size() + " " + this.getString(R.string.sensors));

        // Print each Sensor available using sSensList as the String to be
        // printed
        String sSensList = new String("");
        Sensor tmp;
        int i;
        for (i = 0; i < msensorList.size(); i++) {
            tmp = msensorList.get(i);
            sSensList = sSensList + tmp.getName() + "(" + tmp.getType() + ")" + "\n";
        }
        // if there are sensors available show the list
        if (i > 0) {
            replaceSensorList(sSensList);
        }

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            // @Override
            // public void onCallStateChanged(int state, String incomingNumber) {}
            //
            // @Override
            // public void onCellLocationChanged(CellLocation location) {}
            //
            // @Override
            // public void onDataActivity(int direction) {}
            //
            // @Override
            // public void onDataConnectionStateChanged(int state) {}
            //
            // @Override
            // public void onServiceStateChanged(ServiceState serviceState) {}

            @Override
            public void onSignalStrengthsChanged(final SignalStrength strength) {
                final long timeStamp = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();

                if (!strength.isGsm()) {
                    sb.append("Phone not on GSM network");
                } else {
                    sb.append("GSM signal strength: ").append(strength.getGsmSignalStrength());
                    sb.append(" (error rate: ").append(strength.getGsmBitErrorRate()).append(")\n");
                    sb.append("CDMA RSSI: ").append(strength.getCdmaDbm()).append("dBm");
                    sb.append(" (Ec/Io: ").append(strength.getCdmaEcio()).append(")\n");
                    sb.append("EVDO RSSI: ").append(strength.getEvdoDbm()).append("dBm");
                    sb.append(" (Ec/Io: ").append(strength.getEvdoEcio());
                    sb.append(", SN/R: ").append(strength.getEvdoSnr()).append(")\n");
                    sb.append(strength.toString()).append("\n");
                }

                final String msg = sb.toString();

                updateCellSignalStrength(msg);
                if (currentlyLogging) {
                    logMessage(msg, timeStamp, logFilename);
                    System.out.println(strength.toString());
                    System.out.println(msg);
                }
            }
        };

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * Add a sensor to the list of available sensors
     * @param s description of the sensor
     */
    protected void addToSensorList(String s) {
        sensorsAvailable.setText(sensorsAvailable.getText() + s);
    }

    /**
     * Clear and then replace the sensor list.
     * @param s New content for sensor list
     */
    protected void replaceSensorList(String s) {
        sensorsAvailable.setText(s);
    }

    /**
     * Update the text view which displays cell strength signal information
     * @param s the cell strength signal information
     */
    protected void updateCellSignalStrength(String s) {
        cellSignal.setText(s);
    }

    /**
     * Toggle showing and hiding the list of sensors
     */
    private void toggleSensorList() {
        if (sensorsShowing) {
            sensorsAvailable.setVisibility(View.GONE);
            toggleSensors.setText(R.string.show_sensors);
        } else {
            sensorsAvailable.setVisibility(View.VISIBLE);
            toggleSensors.setText(R.string.hide_sensors);
        }
        sensorsShowing = !sensorsShowing;
    }

    /**
     * Toggle logging of cell signal strength info to a file on the SD card
     */
    private synchronized void toggleLogging() {
        if (currentlyLogging) {
            currentlyLogging = false;
            logInput.setText(R.string.start_recording);
            Toast.makeText(this, "Stopped logging", Toast.LENGTH_SHORT).show();
        } else {
            if (filenameInput.getText() == null) {
                Toast.makeText(this, "Please input a filename", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = filenameInput.getText().toString();
            if (name.trim().length() == 0) {
                Toast.makeText(this, "Please input a filename", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO sanitize input further for security
            logFilename = name;
            currentlyLogging = true;
            logInput.setText(R.string.stop_recording);
            Toast.makeText(this, "Started logging", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Log a message to a file 'data.txt' in the root of the SD storage. Appends to currently
     * existing files.
     * 
     * @param msg The message to log
     * @param timeStamp The timestamp of the message
     * @param filename The name of the file to log this message to.
     */
    private void logMessage(String msg, long timeStamp, String filename) {
        File myFile = new File(Environment.getExternalStorageDirectory().getPath(), filename);
        try {
            // create a filewriter and set append modus to true
            FileWriter fw = new FileWriter(myFile, true);
            msg = "==========\nTimestamp: " + timeStamp + "\n" + msg;
            fw.append(msg);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
