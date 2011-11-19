package com.sector67.space.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.sector67.space.helper.DatabaseHelper;
import com.sector67.space.model.SensorActivity;

public class SensorService extends Service implements SensorEventListener  {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mTemperature;
    private final DatabaseHelper dbHelper = new DatabaseHelper(this);
    private static PowerManager.WakeLock wakeLock;

    
    /**
     * This is the object that receives interactions from clients.
     */
    private final IBinder mBinder = new Binder() {
        @Override
                protected boolean onTransact(int code, Parcel data, Parcel reply,
                        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
    
    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
		@Override
		public void run() {
		    //attempt to release wakelock.
			if (wakeLock != null) wakeLock.release();
			
            long endTime = System.currentTimeMillis() + 15*1000;
            
    		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
            //Start going nuts
            mSensorManager.registerListener(SensorService.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(SensorService.this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
 	       
            //aquire wakelock
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP, SensorService.class.getName());
            wakeLock.acquire();
            
            //"Give it a second, it's going to space"
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
        	//release wakelock
            if (wakeLock != null) {
            	wakeLock.release();
            	wakeLock = null;
            }
            // Done with our work...  stop the service!
            SensorService.this.stopSelf();
		}
    };

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();
		Map<String, String> dataMap = new HashMap<String, String>();
		String sensorType = "";
		if (type == Sensor.TYPE_ACCELEROMETER) {
			sensorType = "Accelerometer";
			dataMap.put("x", Float.toString(event.values[0]));
			dataMap.put("y", Float.toString(event.values[1]));
			dataMap.put("z", Float.toString(event.values[2]));
			Log.d(SensorService.class.getName(), "Acceleromter event detected!  X: "+ event.values[0] + " Y: " + event.values[1] + " Z:" +  event.values[2]);
			mSensorManager.unregisterListener(SensorService.this, mAccelerometer);
		} else if (type == Sensor.TYPE_TEMPERATURE) {
			double fahrenheit = event.values[0] * (9/5) + 32;
			sensorType = "Temperature";
			dataMap.put("temperatire", Float.toString(event.values[0]));
			Log.d(SensorService.class.getName(), "Temperature event detected! "+ event.values[0] + " degrees celcius (" + fahrenheit + " degrees farenheit");
			mSensorManager.unregisterListener(SensorService.this, mTemperature);
		}
		try {
			JSONObject dataObj = new JSONObject(dataMap);
			dbHelper.getSensorDao().create(new SensorActivity(sensorType, new Date(), dataObj.toString()));
		} catch (SQLException e) {
			Log.e(SensorService.class.getName(), "Unable to write to database", e);
		}
	}
    
	@Override
	public IBinder onBind(Intent intent) {
        return mBinder;
	}
	
    public void onCreate() {
        Thread thr = new Thread(null, mTask, "SensorService");
        thr.start();
    }
    
    public void onDestroy() {
    	
    }
    
}
