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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SensorManager mSensorManager;
    private TextView mSensorsTot, mSensorAvailables, mCellSignal;
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
        mSensorsTot = (TextView) findViewById(R.id.sensor_total);
        mSensorAvailables = (TextView) findViewById(R.id.sensor_avail);
        mCellSignal = (TextView) findViewById(R.id.cell_signal);

        mSensorAvailables.setVisibility(View.GONE);
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
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // List of Sensors Available
        List<Sensor> msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        // Print how may Sensors are there
        mSensorsTot.setText(msensorList.size() + " " + this.getString(R.string.sensors));

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
                }

                final String msg = sb.toString();

                updateCellSignalStrength(msg);
                if (currentlyLogging) {
                    logMessage(msg, timeStamp);
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
        mSensorAvailables.setText(mSensorAvailables.getText() + s);
    }

    /**
     * Clear and then replace the sensor list.
     * @param s New content for sensor list
     */
    protected void replaceSensorList(String s) {
        mSensorAvailables.setText(s);
    }

    /**
     * Update the text view which displays cell strength signal information
     * @param s the cell strength signal information
     */
    protected void updateCellSignalStrength(String s) {
        mCellSignal.setText(s);
    }

    /**
     * Toggle showing and hiding the list of sensors
     */
    private void toggleSensorList() {
        if (sensorsShowing) {
            mSensorAvailables.setVisibility(View.GONE);
            toggleSensors.setText(R.string.show_sensors);
        } else {
            mSensorAvailables.setVisibility(View.VISIBLE);
            toggleSensors.setText(R.string.hide_sensors);
        }
        sensorsShowing = !sensorsShowing;
    }

    /**
     * Toggle logging of cell signal strength info to a file on the SD card
     */
    private synchronized void toggleLogging() {
        logInput
                .setText(currentlyLogging ? R.string.start_recording : R.string.stop_recording);
        currentlyLogging = !currentlyLogging;
    }

    /**
     * Log a message to a file 'data.txt' in the root of the SD storage.
     * @param msg The message to log
     * @param timeStamp The timestamp of the message
     */
    private void logMessage(String msg, long timeStamp) {
        File myFile = new File(Environment.getExternalStorageDirectory().getPath(), "data.txt");
        try {
            // create a filewriter and set append modus to true
            FileWriter fw = new FileWriter(myFile, true);
            msg = "==========\nTimestamp: " + timeStamp + "\n" + msg;
            fw.append(msg);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
