package ru.lapteva.geochat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity
implements LocationListener {
	protected static final int ADD_NEW_LINE = 1313;
	private TextView messages = null;       //поле сообщений
	private EditText line = null;           //поле ввода сообщений
	private Location loc = null;            //координаты
	private LocationManager locManager = null;
	private String auth_token = null;
	private String user = null;
	private String password = null;
	private double radius = 0.0;
	private Dialog loginDialog = null;
	private Dialog radiusDialog = null;
	
	private EditText userLine = null;
	private EditText passwordLine = null;
	private EditText radiusLine = null;
	private LinearLayout loginView = null;
	
	private final Handler handler = new Handler () {
		public void handleMessage(Message msg) {
			if (msg.what == ADD_NEW_LINE)
				appendText((String) msg.obj);
		}
	};
	
	private Thread pullMessages = new Thread() {
		private Timer timer = null;
		private long delay = 1000;
		private Date timeBorder = new Date();
		
	    private void parseReply(String reply) {
	    	JSONTokener tokenizer = new JSONTokener(reply);
	    	try {
	    		JSONObject outer = (JSONObject) tokenizer.nextValue();
	    		JSONObject rss = outer.getJSONObject("rss");
	    		JSONObject channels = rss.getJSONObject("channels");
	    		JSONArray itemsar = channels.getJSONArray("items");
	    		JSONObject items = itemsar.optJSONObject(0);
	    		JSONArray mes = items.getJSONArray("items");
	    		for (int i = mes.length()-1; i >= 0; i--)
	    			parseMessage(mes.optJSONObject(i));
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    private void parseMessage(JSONObject mes) {
	    	try {
	    		String str = mes.getString("description");
	    		String datestr = mes.getString("pubDate");
	    		String userstr = mes.getString("user");
	    		SimpleDateFormat parser = new SimpleDateFormat(dateformat);
	    		Date date = parser.parse(datestr);
	    		if ((date.after(timeBorder)) && (!date.equals(timeBorder))) {
	    			timeBorder = date;
	    			Message msg = new Message();
	    			msg.what = ADD_NEW_LINE;
	    			msg.obj = date.toString() + " " + userstr + ": " +str;
	    			handler.sendMessage(msg);
	    		}
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    	} catch (ParseException e) {
				e.printStackTrace();
			}
	    }
		
		public void run() {
	    	if (timer == null) {
	    		timer = new Timer();
	    		timer.schedule(new TimerTask() {
	    			@Override
	    			public void run() {
	    				if (loc != null && radius > 0.0 && auth_token != null) {
	    					String url = serverurl + feedurl;
	    					JSONStringer encoder = new JSONStringer();
	    					try {
	    						encoder.object();
	    						encoder.key("auth_token"); encoder.value(auth_token);
	    						encoder.key("latitude"); encoder.value(loc.getLatitude());
	    						encoder.key("longitude"); encoder.value(loc.getLongitude());
	    						encoder.key("radius"); encoder.value(radius);
	    						encoder.endObject();
	    						parseReply(doRequest(encoder.toString(), url));
	    					} catch (JSONException e) {
	    						e.printStackTrace();
	    					}
	    				}
	    			}	
	    		}, 1000, delay);
	    	}
		}
	};
	
	public final String serverurl = "http://tracks.osll.spb.ru:81";
	public final String loginurl = "/service/login";
	public final String markurl = "/service/apply";
	public final String feedurl = "/service/rss";
	public final String applychannelurl = "/service/addChannel";
	public final String subscribechannelurl = "/service/subscribe";
	public final String chatchannel = "geochatchannel";
	public final String dateformat = "dd MM yyyy HH:mm:ss.SSS";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        line = (EditText) findViewById(R.id.line);
        messages = (TextView) findViewById(R.id.messages);
        
        initGPS(); //инициализация Location Service
        initTimer(); //запускаем таймер
    }
    
    public void onLoginClick(View view) {    			
    	if (loginDialog == null) {
        	DialogInterface.OnClickListener onOkListener = new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				user = userLine.getText().toString();
    				password = passwordLine.getText().toString();
    				doLoginRequest(user, password);
    				doApplyChannel(chatchannel);
    				doSubscribeChannel(chatchannel);
    				dialog.dismiss();
    			}
    		};
    		
    		userLine = new EditText(this);
    		passwordLine = new EditText(this);
    		loginView = new LinearLayout(this);
    		
    		userLine.setSingleLine(true);
    		passwordLine.setSingleLine(true);
    		loginView.setOrientation(LinearLayout.VERTICAL);
    		
    		loginView.addView(userLine);
    		loginView.addView(passwordLine);
    		
    		Builder builder = new Builder(this);
    		loginDialog = builder.setTitle("Login dialog")
    								.setMessage("Please enter login and password:")
    								.setPositiveButton("ok", onOkListener)
    								.setView(loginView).create();
    	}
    	
    	userLine.setText("enter user name");
    	passwordLine.setText("enter password");
    	loginDialog.show();
    }
    
    public void onRadiusClick(View view) {
    	if (radiusDialog == null) {
        	DialogInterface.OnClickListener onOkListener = new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				radius = Double.parseDouble(radiusLine.getText().toString());
    				dialog.dismiss();
    			}
    		};
    		
    		radiusLine = new EditText(this);
    		
    		Builder builder = new Builder(this);
    		radiusDialog = builder.setTitle("Radius dialog")
    								.setMessage("Please enter radius:")
    								.setPositiveButton("ok", onOkListener)
    								.setView(radiusLine).create();
    	}
    	radiusDialog.show();
    }
    
    private void initTimer() {
    	pullMessages.start();
    }
    
    private void initGPS() {
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	if (loc == null)
    		loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    
    private void doSubscribeChannel(String channel) {
    	String url = serverurl + subscribechannelurl;
    	JSONStringer encoder = new JSONStringer();
    	try {
    		encoder.object();
    		encoder.key("auth_token"); encoder.value(auth_token);
    		encoder.key("channel"); encoder.value(channel);
    		encoder.endObject();
    		doRequest(encoder.toString(), url);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    }
    
    private void doApplyChannel(String channel) {
    	String url = serverurl + applychannelurl;
    	JSONStringer encoder = new JSONStringer();
    	try {
    		encoder.object();
    		encoder.key("auth_token"); encoder.value(auth_token);
    		encoder.key("name"); encoder.value(channel);
    		encoder.key("description"); encoder.value("geo chat channel");
    		encoder.key("url"); encoder.value("http://osll.spb.ru/");
    		encoder.key("activeRadius"); encoder.value(3000);
    		encoder.endObject();
    		doRequest(encoder.toString(), url);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    }
    
	private void doLoginRequest(String user, String password) {
		String url = serverurl + loginurl;
		JSONStringer encoder = new JSONStringer();
		try {
			encoder.object();
			encoder.key("login"); encoder.value(user);
			encoder.key("password"); encoder.value(password);
			encoder.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String response = doRequest(encoder.toString(), url);
		try {
			JSONObject obj = (JSONObject) new JSONTokener(response).nextValue();
			auth_token = obj.getString("auth_token");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
    
    public void onSendClick(View view) {
    	switch (view.getId()) {
    	case R.id.send:
    		if (auth_token == null) {
    			Toast.makeText(this, "Login please...", Toast.LENGTH_LONG).show();
    			return;
    		}
    		String message = line.getText().toString();
    		if (!message.trim().equals(""))
    			doSendMessage(message);
    		line.setText("");
    		break;
    	}
    }
    
    private void appendText(String mes) {
    	messages.setText(messages.getText() + mes + "\n");
    	messages.postInvalidate();
    }

	@Override
	public void onLocationChanged(Location arg0) {
		loc = arg0;
		if (loc != null) {
			appendText("New coordinates: " + loc.getLatitude() 
					+ "," + loc.getLongitude());
		}
	}
	
	private String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat(dateformat);
		return formatter.format(new Date());
	}
	
	private void doSendMessage(String message) {
		if (loc != null) {
			String url = serverurl + markurl;
			JSONStringer encoder = new JSONStringer();
			try {
				encoder.object();
				encoder.key("auth_token"); encoder.value(auth_token);
				encoder.key("channel"); encoder.value(chatchannel);
				encoder.key("description"); encoder.value(message);
				encoder.key("latitude"); encoder.value(loc.getLatitude());
				encoder.key("link"); encoder.value("unknown");
				encoder.key("longitude"); encoder.value(loc.getLongitude());
				encoder.key("time"); encoder.value(getTime());
				encoder.key("title"); encoder.value("chat message");
				encoder.endObject();
				doRequest(encoder.toString(), url);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, "Location is not accessible", Toast.LENGTH_LONG).show();
		}
	}
	
	private String doRequest(String query, String url) {
		String response = null;
		try {
			URL	server = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) server.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.connect();
			
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(query);
			writer.close();
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStreamReader reader = new InputStreamReader(connection.getInputStream());
				StringBuffer data = new StringBuffer();
				int c;
				while ((c = reader.read()) != -1)
					data.append((char) c);
				response = data.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
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