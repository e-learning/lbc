package ru.leti.lapteva;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

//TODO: create class-wrapper for LocationService with notification API

public class LbcActivity extends Activity
implements LocationListener {
	/** Called when the activity is first created. */
	
	private TextView tv;
	private Application app;
	private Context con;
	private LocationManager lm;
	private Location loc;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        app = getApplication();
        con = app.getApplicationContext();
        lm = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc != null)
        	tv.setText(loc.toString());
        else
        	tv.setText("unlonown");
        setContentView(tv);
    }

	public void onLocationChanged(Location arg0) {
		loc = arg0;
		if (loc != null)
			tv.setText(loc.toString());
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {	
		//nothing TODO
	}
}