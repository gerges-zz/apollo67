package com.sector67.space;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.LocationService;


public class RecoveryActivity extends Activity implements TextToSpeech.OnInitListener {
    private PendingIntent mCamcorderSender;
    private BroadcastReceiver locationReciever;
    private static int ALARM_TIME = 120;
    private Timer camAlarm;
    private Timer ringAlarm;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private TextToSpeech mTts;
    private boolean speechReady;



	public RecoveryActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.recovery);
        
        Log.d(LaunchActivity.class.getName(), "Entering Recovery Activity");
        
        mTts = new TextToSpeech(this, this);
        
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 300*1000);

        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
		
		camAlarm = new Timer();
		ringAlarm = new Timer();
		
		//Wait for the right moment
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mCamcorderSender);
		scheduler.schedule(new Callable<AlarmManager>(){
            public AlarmManager call() { 
        			final AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        			am.cancel(mCamcorderSender);
					return am;
            }
          }, 300, SECONDS);
        
		scheduler.scheduleAtFixedRate(new Runnable(){
	        public void run() { 
	        	Log.d(RecoveryActivity.class.getName(), "Ding Dong");
	       		MediaPlayer mPlayer = MediaPlayer.create(RecoveryActivity.this, R.raw.loudbeep);
	    		mPlayer.start();
	           }
	         }, 0, ALARM_TIME, SECONDS);
			
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
	        }
	    }
	 
	protected void onResume() {
		super.onResume();
    }
	
	public void onDestroy() {
		super.onDestroy();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mCamcorderSender);
		if(null != camAlarm) {			
			camAlarm.cancel();		
		}
		if(null != ringAlarm) {
			ringAlarm.cancel();
		}
	}
	
	public void onPause() {
		super.onPause();
		if(null != camAlarm) {			
			camAlarm.cancel();		
		}
		if(null != ringAlarm) {
			ringAlarm.cancel();
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
;