package com.sector67.space;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.inject.Inject;

public class MainActivity extends RoboActivity {
	private @Inject SharedPreferences sharedPreferences;
	private @InjectView(R.id.triggerText) EditText triggerText; 
	private @InjectView(R.id.savePrefs) Button savePrefs;
	private @InjectView(R.id.currentTrigger) TextView currentTrigger;
	private @InjectView(R.id.primerButton) ToggleButton primerButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        updateDisplay("trigger", "Apollo67 Launch", currentTrigger);
        
        savePrefs.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View view) {
			  String newTriggerText = triggerText.getText().toString().trim();
			  if(!newTriggerText.equals("")) {
				  savePref("trigger", newTriggerText);
			  }
			  updateDisplay("trigger", "Apollo67 Launch", currentTrigger);
		  }
        });
        primerButton.setOnClickListener(new ToggleButton.OnClickListener(){
  		  public void onClick(View view) {
  			   savePref("primer", Boolean.toString(primerButton.isChecked()));
  			  }
  	  	});

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