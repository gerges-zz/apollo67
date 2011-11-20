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


public class RecoveryActivity extends Activity {
    private PendingIntent mCamcorderSender;
    private static int ALARM_TIME = 120*1000;
    private Timer alarmRepeat;


	public RecoveryActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.recovery);
        
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);

        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
		
		alarmRepeat = new Timer();

		alarmRepeat.scheduleAtFixedRate(new TimerTask(){
           public void run() { 
       		MediaPlayer mPlayer = MediaPlayer.create(RecoveryActivity.this, R.raw.loudbeep);
    		mPlayer.start();
           }
         }, 0, ALARM_TIME);
		
		//Wait for the right moment
        long firstTime = SystemClock.elapsedRealtime() + 31000;
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 500*1000, mCamcorderSender);
		
    }

	protected void onResume() {
		super.onResume();
    }
	
	public void onDestroy() {
		super.onDestroy();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mCamcorderSender);
		if(null != alarmRepeat) {			
			alarmRepeat.cancel();		
		}
	}
	
	public void onPause() {
		super.onPause();
		if(null != alarmRepeat) {			
			alarmRepeat.cancel();		
		}
	}
}
;