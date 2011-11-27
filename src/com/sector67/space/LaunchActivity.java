package com.sector67.space;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.CameraReciever;
import com.sector67.space.service.LocationService;


public class LaunchActivity extends Activity {
    private PendingIntent mLocationAlarmSender;
    private PendingIntent mCamcorderSender;
    private static PowerManager.WakeLock wakeLock;

	public LaunchActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.launch);
        
        Log.d(LaunchActivity.class.getName(), "Entering Launch Activity");
        
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 300*1000);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);

        
		mLocationAlarmSender = PendingIntent.getService(LaunchActivity.this,
                0, new Intent(LaunchActivity.this, LocationService.class), 0);
		
		//Wait for the right moment
        long firstTime = SystemClock.elapsedRealtime() + 31000;
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mLocationAlarmSender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mCamcorderSender);
		MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.launch_countdown);
		mPlayer.start();
		
        Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
               nextActivity();
            }

         }, 331*1000);

    }

	protected void onResume() {
		super.onResume();
    }
	
	public void onDestroy() {
		super.onDestroy();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mCamcorderSender);
	}
	
	private void stopCameraAndCamcorder() {
		//Stop Camcorder
		Intent stopCamcorderReciever = new Intent(LaunchActivity.this, CamcorderReciever.class);
		stopCamcorderReciever.putExtra("action", "stop");
		sendBroadcast(stopCamcorderReciever);
		//Stop Camera
		Intent stopCameraReciever = new Intent(LaunchActivity.this, CameraReciever.class);
		stopCameraReciever.putExtra("action", "stop");
		sendBroadcast(stopCameraReciever);
	}

	private void nextActivity() {
		//aquire wakelock
       PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
       wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, LaunchActivity.class.getName());
       wakeLock.acquire();
       
	   Intent spaceIntent = new Intent(getBaseContext(), SpaceActivity.class);
       stopCameraAndCamcorder();
	   startActivity(spaceIntent);
	   finish();
	   
   		//release wakelock
       if (wakeLock != null) wakeLock.release();
	}
}
