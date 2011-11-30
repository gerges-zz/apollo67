package com.sector67.space;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import roboguice.activity.RoboActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Window;

import com.google.inject.Inject;
import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.CameraReciever;
import com.sector67.space.service.LocationService;


public class LaunchActivity extends RoboActivity implements TextToSpeech.OnInitListener {
	@Inject AlarmManager alarmManager;
	private PendingIntent mLocationAlarmSender;
    private PendingIntent mCamcorderSender;
    private static PowerManager.WakeLock wakeLock;
    private BroadcastReceiver locationReciever;
    private TextToSpeech mTts;
    private boolean speechReady;

	public LaunchActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.launch);
        
        Log.d(LaunchActivity.class.getName(), "Entering Launch Activity");
        
        mTts = new TextToSpeech(this, this);
        
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 300*1000);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
		mLocationAlarmSender = PendingIntent.getService(LaunchActivity.this, 0, new Intent(LaunchActivity.this, LocationService.class), 0);
		
		//Wait for the right moment
        long firstTime = SystemClock.elapsedRealtime() + 31000;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mLocationAlarmSender);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mCamcorderSender);
		MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.launch_countdown);
		mPlayer.start();
		
        Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
               nextActivity();
            }

         }, 331*1000);
        
		//Register for location updates
        IntentFilter locationFilter;
        locationFilter = new IntentFilter(LocationService.LOCATION_UPDATE);
        locationReciever = new LocationServiceReciever();
        registerReceiver(locationReciever, locationFilter);

    }

	protected void onResume() {
		super.onResume();
    }
	
	public void onDestroy() {
		super.onDestroy();
		alarmManager.cancel(mCamcorderSender);
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
	
	 public class LocationServiceReciever extends BroadcastReceiver {
	      @Override
	        public void onReceive(Context context, Intent intent) {
	                double altitude = intent.getDoubleExtra(LocationService.ALTITUDE, 0);
	                double longitude = intent.getDoubleExtra(LocationService.LONGITUDE, 0);
	                double lattitude = intent.getDoubleExtra(LocationService.LATTITUDE, 0);
	                String message = "loc, longitude " + Double.toString(longitude) + ", lattitude " + Double.toString(lattitude) + ", altitude " + Double.toString(altitude);
	                if(null != mTts && speechReady) {
	                	mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
	                }
	        }
	    }
	 
	@Override
	public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(SpaceActivity.class.getName(), "Language is not available.");
            }
            speechReady = true;
        } else {
            // Initialization failed.
            Log.e(SpaceActivity.class.getName(), "Could not initialize TextToSpeech.");
        }
	}
}
