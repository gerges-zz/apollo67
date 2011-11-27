package com.sector67.space;

import java.util.Timer;
import java.util.TimerTask;

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
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.LocationService;


public class RecoveryActivity extends Activity {
    private PendingIntent mCamcorderSender;
    private BroadcastReceiver locationReciever;
    private static int ALARM_TIME = 120*1000;
    private Timer alarmRepeat;



	public RecoveryActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.recovery);
        
        Log.d(LaunchActivity.class.getName(), "Entering Recovery Activity");
        
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 300*1000);

        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
		alarmRepeat = new Timer();

		//Wait for the right moment
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mCamcorderSender);
        alarmRepeat.schedule(new TimerTask(){
            public void run() { 
        			AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        			am.cancel(mCamcorderSender);
            }
          }, 0, 300*1000);
        
		alarmRepeat.scheduleAtFixedRate(new TimerTask(){
	        public void run() { 
	       		MediaPlayer mPlayer = MediaPlayer.create(RecoveryActivity.this, R.raw.loudbeep);
	    		mPlayer.start();
	           }
	         }, 330*1000, ALARM_TIME);
			
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
                double lattitude = intent.getDoubleExtra(LocationService.LATTITUDE, 0);
                double longitude = intent.getDoubleExtra(LocationService.LONGITUDE, 0);
                double altitude = intent.getDoubleExtra(LocationService.ALTITUDE, 0);
               
                //Prepare for recovery, send some texts
                SmsManager sms = SmsManager.getDefault();
                String message = lattitude + ", " + longitude + " at " + altitude + " meters";
                sms.sendTextMessage("9206981905", null, message, null, null);
	        }
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