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
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private SharedPreferences sharedPreferences;
	private EditText triggerText; 
	private Button savePrefs;
	private TextView currentTrigger;
	private ToggleButton primerButton;
	private Button.OnClickListener saveOnClickListener = new Button.OnClickListener(){
		  @Override
		  public void onClick(View arg0) {
			  String newTriggerText = triggerText.getText().toString().trim();
			  if(!newTriggerText.equals("")) {
				  savePref("trigger", newTriggerText);
			  }
			  updateDisplay("trigger", "Apollo67 Launch", currentTrigger);
		  }
     };
 	private ToggleButton.OnClickListener primerClickListener = new ToggleButton.OnClickListener(){
		  @Override
		  public void onClick(View arg0) {
		   savePref("primer", Boolean.toString(primerButton.isChecked()));
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
        currentTrigger = (TextView)findViewById(R.id.currentTrigger);
        primerButton = (ToggleButton)findViewById(R.id.primerButton);

        savePrefs.setOnClickListener(saveOnClickListener);
        primerButton.setOnClickListener(primerClickListener);
        
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