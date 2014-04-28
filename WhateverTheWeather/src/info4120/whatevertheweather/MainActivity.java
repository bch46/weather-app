package info4120.whatevertheweather;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

    private SensorManager mSensorManager;
    private TextView mSensorsTot, mSensorAvailables, mCellSignal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the texts fields of the layout and setup to invisible
        mSensorsTot = (TextView) findViewById(R.id.sensor_total);
        mSensorAvailables = (TextView) findViewById(R.id.sensor_avail);
        mCellSignal = (TextView) findViewById(R.id.cell_signal);

        // Get the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // List of Sensors Available
        List<Sensor> msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        // Print how may Sensors are there
        mSensorsTot.setText(msensorList.size() + " " + this.getString(R.string.sensors) + "!");

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
//            @Override
//            public void onCallStateChanged(int state, String incomingNumber) {}
//
//            @Override
//            public void onCellLocationChanged(CellLocation location) {}
//
//            @Override
//            public void onDataActivity(int direction) {}
//
//            @Override
//            public void onDataConnectionStateChanged(int state) {}
//
//            @Override
//            public void onServiceStateChanged(ServiceState serviceState) {}

            @Override
            public void onSignalStrengthsChanged(final SignalStrength strength) {
                MainActivity.this.runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        System.out.println("onSignalStrengthsChanged");
                        MainActivity.this.updateCellSignalStrength(strength.toString());
                    }
                });
            }
        };

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    protected void addToSensorList(String s) {
        mSensorAvailables.setText(mSensorAvailables.getText() + s);
    }

    protected void replaceSensorList(String s) {
        mSensorAvailables.setText(s);
    }

    protected void updateCellSignalStrength(String s) {
        mCellSignal.setText(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
