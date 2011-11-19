package com.sector67.space;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SharedPreferences sharedPreferences;
	private EditText triggerText; 
	private Button savePrefs;
	private TextView currentTrigger;
	private Button.OnClickListener saveOnClickListener = new Button.OnClickListener(){
		  @Override
		  public void onClick(View arg0) {
		   savePref("trigger", triggerText.getText().toString());
	       updateDisplay("trigger", "Apollo67 Launch", currentTrigger);
		  }
     };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        triggerText = (EditText)findViewById(R.id.triggerText);
        savePrefs = (Button)findViewById(R.id.savePrefs);
        savePrefs.setOnClickListener(saveOnClickListener);
        currentTrigger = (TextView)findViewById(R.id.currentTrigger);
        
        updateDisplay("trigger", "Apollo67 Launch", currentTrigger);

    }
    
    private void updateDisplay (String prefName, String defaultText, TextView tv){
    	String prefText = sharedPreferences.getString(prefName, defaultText);
    	tv.setText(prefText);
    }
    
    private void savePref(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
      
}