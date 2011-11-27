package com.sector67.space;

import java.util.Locale;

import android.app.Activity;
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

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.CameraReciever;
import com.sector67.space.service.LocationService;
import com.sector67.space.service.SensorService;


public class SpaceActivity extends Activity implements TextToSpeech.OnInitListener {
	private PendingIntent mSensorAlarmSender;
    private PendingIntent mCameraSender;
    private PendingIntent mCamcorderSender;
    private BroadcastReceiver locationReciever;
    private static PowerManager.WakeLock wakeLock;
    private boolean hasEnded = false;
    private double ALTITUDE_CAP = 15240;
    private TextToSpeech mTts;
    private boolean speechReady;
    
	public SpaceActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.space);
        
        Log.d(LaunchActivity.class.getName(), "Entering Space Activity");
        
        mTts = new TextToSpeech(this, this);
        
        Intent cameraIntent = new Intent(getBaseContext(), CameraReciever.class);
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 30*1000);

        
        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mSensorAlarmSender = PendingIntent.getService(SpaceActivity.this,
                0, new Intent(SpaceActivity.this, SensorService.class), 0);
		mCameraSender = PendingIntent.getBroadcast(getBaseContext(), 0, cameraIntent, 0);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
        
		//we run a tight schedule.
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mSensorAlarmSender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mCameraSender);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime + 15000, 600*1000, mCamcorderSender);
		
		//Register for location updates
        IntentFilter locationFilter;
        locationFilter = new IntentFilter(LocationService.LOCATION_UPDATE);
        locationReciever = new LocationServiceReciever();
        registerReceiver(locationReciever, locationFilter);

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
	                
	                if(altitude > ALTITUDE_CAP) {
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
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mSensorAlarmSender);
		am.cancel(mCameraSender);
		am.cancel(mCamcorderSender);

	}
	
	private void stopCameraAndCamcorder() {
		//Stop Camcorder
		Intent stopCamcorderReciever = new Intent(SpaceActivity.this, CamcorderReciever.class);
		stopCamcorderReciever.putExtra("action", "stop");
		sendBroadcast(stopCamcorderReciever);
		//Stop Camera
		Intent stopCameraReciever = new Intent(SpaceActivity.this, CameraReciever.class);
		stopCameraReciever.putExtra("action", "stop");
		sendBroadcast(stopCameraReciever);
	}
	
	private void nextActivity() {
		//aquire wakelock
           PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
           wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, LaunchActivity.class.getName());
           wakeLock.acquire();
           
    	   Intent spaceIntent = new Intent(SpaceActivity.this, PeakActivity.class);
           stopCameraAndCamcorder();
    	   startActivity(spaceIntent);
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
