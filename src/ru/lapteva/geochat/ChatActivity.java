package ru.lapteva.geochat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	private EditText user;
	private EditText line;
	private TextView messages;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        user = (EditText) findViewById(R.id.user);
        line = (EditText) findViewById(R.id.line);
        messages = (TextView) findViewById(R.id.messages);
        
        initGPS();
    }
    
    private void initGPS() {
    	
    }
    
    public void onSendClick(View view) {
    	switch (view.getId()) {
    	case R.id.send:
    		String name = user.getText().toString();
    		if (name.trim().equals("")) {
    			Toast.makeText(this, "Please enter user name... Дурень.", Toast.LENGTH_LONG).show();
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
}