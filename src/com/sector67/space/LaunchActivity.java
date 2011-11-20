package com.sector67.space;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.LocationService;


public class LaunchActivity extends Activity {
    private PendingIntent mLocationAlarmSender;

	public LaunchActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.launch);
        
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 300*1000);
        
		mLocationAlarmSender = PendingIntent.getService(LaunchActivity.this,
                0, new Intent(LaunchActivity.this, LocationService.class), 0);
		
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mLocationAlarmSender);
		MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.launch_countdown);
		mPlayer.start();
		
		//Wait for the right moment
		startService(camcorderIntent);
		
        Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
               Intent spaceIntent = new Intent(getBaseContext(), SpaceActivity.class);
        	   startActivity(spaceIntent);
        	   finish();
            }
         }, 300*1000);

    }

	protected void onResume() {
		super.onResume();
    }
	
	public void onDestroy() {
		super.onDestroy();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mLocationAlarmSender);
	}
}
