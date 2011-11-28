package ru.leti.lapteva;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class LbcActivity extends Activity
implements LocationListener {
	private TextView tv;
	private Application app;
	private Context con;
	private LocationManager lm;
	private Location loc;
	
	static String loginrequest = "{\"login\":\"Paul\",\"password\":\"test\"}";
	static String url = "http://tracks.osll.spb.ru:81/";
	static String login = "service/login";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        app = getApplication();
        con = app.getApplicationContext();
        GPSInit();
        
        tv.setText(tv.getText() + doLoginRequest());

        setContentView(tv);
    }
    
    private void GPSInit() {
    	lm = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
    
    private String doLoginRequest() {
    	return doRequest(url + login, loginrequest);
    }
    
    private String doRequest(String url, String query) {
    	String response = null;
    	try {
    		URL svr = new URL(url);
    		HttpURLConnection con = (HttpURLConnection) svr.openConnection();
    		con.setRequestMethod("POST");
    		con.setDoOutput(true);
    		con.setDoInput(true);
    		con.connect();
    		
    		OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
    		writer.write(query);
    		writer.close();
    		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
    			InputStream is = con.getInputStream();
    			InputStreamReader reader = new InputStreamReader(is);
    			StringBuffer data = new StringBuffer();
    			int c;
    			while ((c = reader.read()) != -1)
    				data.append((char) c);   		
    			response = data.toString();
    		} else {
    			response = "Server does not response";
    		}
		} catch (MalformedURLException e) {
			response = "wrong url: " + e.getMessage();
		} catch (IOException e) {
			response = "IOException: " + e.getMessage();
    	} catch (Exception e) {	
    		response = "other: " + e.getMessage();
    	}
    	
    	return response;
    }

	public void onLocationChanged(Location arg0) {
		loc = arg0;
		if (loc != null)
			tv.setText(tv.getText() + loc.toString());
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {	
	}
}