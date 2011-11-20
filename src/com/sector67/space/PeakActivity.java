package com.sector67.space;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.CameraReciever;
import com.sector67.space.service.LocationService;
import com.sector67.space.service.SensorService;


public class PeakActivity extends Activity {
	private PendingIntent mSensorAlarmSender;
    private PendingIntent mLocationAlarmSender;
    private PendingIntent mCameraSender;
    private PendingIntent mCamcorderSender;
    private BroadcastReceiver locationReciever;
    private double ALTITUDE_MIN = 12192;

	public PeakActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.space);
        
        Intent cameraIntent = new Intent(getBaseContext(), CameraReciever.class);
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);
        camcorderIntent.putExtra("timeToRecord", 60*1000);

        
        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mSensorAlarmSender = PendingIntent.getService(PeakActivity.this,
                0, new Intent(PeakActivity.this, SensorService.class), 0);
		mLocationAlarmSender = PendingIntent.getService(PeakActivity.this,
                0, new Intent(PeakActivity.this, LocationService.class), 0);
		mCameraSender = PendingIntent.getBroadcast(getBaseContext(), 0, cameraIntent, 0);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
        
		//we run a tight schedule.
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mSensorAlarmSender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mLocationAlarmSender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30*1000, mCameraSender);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300*1000, mCamcorderSender);
		
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
	                if(altitude < ALTITUDE_MIN) {
	                	Intent nextIntent = new Intent(PeakActivity.this, FallingActivity.class);
	                	startActivity(nextIntent);
	                	finish();
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
		am.cancel(mLocationAlarmSender);
		am.cancel(mCameraSender);
		am.cancel(mCamcorderSender);
		
		//unregister reciever
        unregisterReceiver(locationReciever);

	}
	
}
