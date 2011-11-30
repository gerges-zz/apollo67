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
import com.sector67.space.service.SensorService;


public class PeakActivity extends RoboActivity  implements TextToSpeech.OnInitListener {
	@Inject AlarmManager alarmManager;
	private PendingIntent mSensorAlarmSender;
    private PendingIntent mCameraSender;
    private PendingIntent mCamcorderSender;
    private BroadcastReceiver locationReciever;
    private boolean hasEnded = false;
    private static PowerManager.WakeLock wakeLock;
    private double ALTITUDE_MIN = 12192;
    private TextToSpeech mTts;
    private boolean speechReady;

	public PeakActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.space);
        
        Log.d(LaunchActivity.class.getName(), "Entering Peak Activity");
        
        mTts = new TextToSpeech(this, this);
        
        Intent cameraIntent = new Intent(getBaseContext(), CameraReciever.class);
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 300*1000);

        
        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mSensorAlarmSender = PendingIntent.getService(PeakActivity.this,
                0, new Intent(PeakActivity.this, SensorService.class), 0);
		mCameraSender = PendingIntent.getBroadcast(getBaseContext(), 0, cameraIntent, 0);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
        
		//we run a tight schedule.
        long firstTime = SystemClock.elapsedRealtime();
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mSensorAlarmSender);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 500*1000, mCamcorderSender);
        
        Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
       	        long firstTime = SystemClock.elapsedRealtime();
       	        alarmManager.cancel(mCamcorderSender);
       	        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30*1000, mCameraSender);
            }
         }, 330*1000);

		
		//Register for location updates
        IntentFilter locationFilter;
        locationFilter = new IntentFilter(LocationService.LOCATION_UPDATE);
        locationReciever = new LocationServiceReciever();
        registerReceiver(locationReciever, locationFilter);

    }
	 public class LocationServiceReciever extends BroadcastReceiver {
	      @Override
	        public void onReceive(Context context, Intent intent)//this method receives broadcast messages. Be sure to modify AndroidManifest.xml file in order to enable message receiving
	        {
		          double altitude = intent.getDoubleExtra(LocationService.ALTITUDE, 0);
		          double longitude = intent.getDoubleExtra(LocationService.LONGITUDE, 0);
		          double lattitude = intent.getDoubleExtra(LocationService.LATTITUDE, 0);
		          String message = "loc, longitude " + Double.toString(longitude) + ", lattitude " + Double.toString(lattitude) + ", altitude " + Double.toString(altitude);
		          if(null != mTts && speechReady) {
		          	mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		          }
                if(altitude < ALTITUDE_MIN) {
                	if(!hasEnded) {
                		nextActivity();
	                	hasEnded = true;
                	}
                }
	                
	        }
	    }

	protected void onResume() {
		super.onResume();
    }
	
	public void onDestroy() {
		super.onDestroy();
		alarmManager.cancel(mSensorAlarmSender);
		alarmManager.cancel(mCameraSender);
		alarmManager.cancel(mCamcorderSender);
	}
	
	private void stopCameraAndCamcorder() {
		//Stop Camcorder
		Intent stopCamcorderReciever = new Intent(PeakActivity.this, CamcorderReciever.class);
		stopCamcorderReciever.putExtra("action", "stop");
		sendBroadcast(stopCamcorderReciever);
		//Stop Camera
		Intent stopCameraReciever = new Intent(PeakActivity.this, CameraReciever.class);
		stopCameraReciever.putExtra("action", "stop");
		sendBroadcast(stopCameraReciever);
	}
	
	private void nextActivity() {
		//aquire wakelock
       PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
       wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, LaunchActivity.class.getName());
       wakeLock.acquire();
       
	   Intent nextIntent = new Intent(PeakActivity.this, FallingActivity.class);
       stopCameraAndCamcorder();
	   startActivity(nextIntent);
	   finish();
	   
   		//release wakelock
       if (wakeLock != null) wakeLock.release();
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
