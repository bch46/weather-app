package info4120.whatevertheweather;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private SensorManager mSensorManager;
	TextView mSensorsTot, mSensorAvailables;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Get the texts fields of the layout and setup to invisible
		mSensorsTot = (TextView) findViewById(R.id.sensor_total);
		mSensorAvailables = (TextView) findViewById(R.id.sensor_avail);

		// Get the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// List of Sensors Available
		List<Sensor> msensorList = mSensorManager
				.getSensorList(Sensor.TYPE_ALL);

		// Print how may Sensors are there
		mSensorsTot.setText(msensorList.size() + " "
				+ this.getString(R.string.sensors) + "!");

		// Print each Sensor available using sSensList as the String to be
		// printed
		String sSensList = new String("");
		Sensor tmp;
		int x, i;
		for (i = 0; i < msensorList.size(); i++) {
			tmp = msensorList.get(i);
			sSensList = " " + sSensList + tmp.getName(); // Add the sensor name
															// to the string of
															// sensors available
		}
		// if there are sensors available show the list
		if (i > 0) {
			sSensList = getString(R.string.sensors) + ":" + sSensList;
			mSensorAvailables.setText(sSensList);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
