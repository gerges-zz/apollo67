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
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.CameraReciever;
import com.sector67.space.service.LocationService;
import com.sector67.space.service.SensorService;


public class FallingActivity extends Activity implements TextToSpeech.OnInitListener {
	private PendingIntent mSensorAlarmSender;
    private PendingIntent mCameraSender;
    private PendingIntent mCamcorderSender;
    private BroadcastReceiver locationReciever;
    private boolean hasEnded = false;
    private double ALTITUDE_MIN = 1524;
    private TextToSpeech mTts;
    private boolean speechReady;

	public FallingActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.descent);
        
        Log.d(LaunchActivity.class.getName(), "Entering Falling Activity");
        
        mTts = new TextToSpeech(this, this);
        
        Intent cameraIntent = new Intent(getBaseContext(), CameraReciever.class);
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 30*1000);

        
        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mSensorAlarmSender = PendingIntent.getService(FallingActivity.this,
                0, new Intent(FallingActivity.this, SensorService.class), 0);
		mCameraSender = PendingIntent.getBroadcast(getBaseContext(), 0, cameraIntent, 0);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
        
		//we run a tight schedule.
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 45*1000, mSensorAlarmSender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mCameraSender);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime + 15000, 300*1000, mCamcorderSender);
		
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
            //Prepare for recovery, send some texts
            SmsManager sms = SmsManager.getDefault();
            String smsMessage = lattitude + ", " + longitude + " at " + altitude + " meters";
            sms.sendTextMessage("9206981905", null, smsMessage, null, null);
        
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
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mCameraSender);
		am.cancel(mCamcorderSender);
		am.cancel(mSensorAlarmSender);

	}
	
	private void stopCameraAndCamcorder() {
		//Stop Camcorder
		Intent stopCamcorderReciever = new Intent(FallingActivity.this, CamcorderReciever.class);
		stopCamcorderReciever.putExtra("action", "stop");
		sendBroadcast(stopCamcorderReciever);
		//Stop Camera
		Intent stopCameraReciever = new Intent(FallingActivity.this, CameraReciever.class);
		stopCameraReciever.putExtra("action", "stop");
		sendBroadcast(stopCameraReciever);
	}
	
	
	private void nextActivity() {
		//aquire wakelock
       PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
       PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, LaunchActivity.class.getName());
       wakeLock.acquire();
       
	   Intent nextIntent = new Intent(FallingActivity.this, RecoveryActivity.class);
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
