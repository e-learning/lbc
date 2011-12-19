package ru.lapteva.geochat;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity
implements LocationListener {
	private EditText user = null;           //user name поле
	private EditText line = null;           //поле ввода сообщения
	private TextView messages = null;       //поле сообщений
	private EditText radius = null;         //радиус действия чата
	private Location loc = null;            //координаты
	private LocationManager locManager = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        user = (EditText) findViewById(R.id.user);
        line = (EditText) findViewById(R.id.line);
        messages = (TextView) findViewById(R.id.messages);
        radius = (EditText) findViewById(R.id.radiusEdit);
        
        initGPS(); //инициализация Location Service
    }
    
    private void initGPS() {
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	if (loc == null)
    		loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    
    public void onSendClick(View view) {
    	switch (view.getId()) {
    	case R.id.send:
    		String name = user.getText().toString();
    		if (name.trim().equals("")) {
    			Toast.makeText(this, "Please enter user name...", Toast.LENGTH_LONG).show();
    			return;
    		}
    		appendText(name + ": " + line.getText() + "\n");
    		line.setText("");
    		break;
    	}
    }
    
    private void appendText(String mes) {
    	messages.append(mes);
    }

	@Override
	public void onLocationChanged(Location arg0) {
		loc = arg0;
		if (loc != null) {
			appendText("New coordinates: " + loc.getLatitude() 
					+ "," + loc.getLongitude());
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// nothing to do
	}

	@Override
	public void onProviderEnabled(String provider) {
		// nothing to do
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// nothing to do
	}
}