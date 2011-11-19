package com.sector67.space;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;

import com.sector67.space.service.CamcorderReciever;
import com.sector67.space.service.CameraReciever;
import com.sector67.space.service.LocationService;
import com.sector67.space.service.SensorService;


public class SpaceActivity extends Activity {
	private PendingIntent mSensorAlarmSender;
    private PendingIntent mLocationAlarmSender;
    private PendingIntent mCameraSender;
    private PendingIntent mCamcorderSender;


	public SpaceActivity() {

	}
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.space);
        
        Intent cameraIntent = new Intent(getBaseContext(), CameraReciever.class);
        Intent camcorderIntent = new Intent(getBaseContext(), CamcorderReciever.class);

        
        // Create IntentSenders that will launch our service, to be scheduled with the alarm manager.
		mSensorAlarmSender = PendingIntent.getService(SpaceActivity.this,
                0, new Intent(SpaceActivity.this, SensorService.class), 0);
		mLocationAlarmSender = PendingIntent.getService(SpaceActivity.this,
                0, new Intent(SpaceActivity.this, LocationService.class), 0);
		mCameraSender = PendingIntent.getBroadcast(getBaseContext(), 0, cameraIntent, 0);
		mCamcorderSender = PendingIntent.getBroadcast(getBaseContext(), 0, camcorderIntent, 0);
        
		//we run a tight schedule.
        long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30*1000, mSensorAlarmSender);
        //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30*1000, mLocationAlarmSender);
        //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30*1000, mCameraSender);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60*1000, mCamcorderSender);

    }

	protected void onResume() {
		super.onResume();
		

    }
}
