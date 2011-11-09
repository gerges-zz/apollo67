package com.sector67.space.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mTemperature;
    
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
            long endTime = System.currentTimeMillis() + 15*1000;
            
    		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
            //Start going nuts
            mSensorManager.registerListener(SensorService.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(SensorService.this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
 	       
            //"Give it a second, it's going to space"
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
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
		if (type == Sensor.TYPE_ACCELEROMETER) {
        	//log.debug("Location found! " + "Acceleromter event detected!  X: "+ event.values[0] + " Y: " + event.values[1] + " Z:" +  event.values[2]);
			Log.d("SensorService", "Acceleromter event detected!  X: "+ event.values[0] + " Y: " + event.values[1] + " Z:" +  event.values[2]);
			mSensorManager.unregisterListener(SensorService.this, mAccelerometer);
		} else if (type == Sensor.TYPE_TEMPERATURE) {
			double fahrenheit = event.values[0] * (9/5) + 32;
			//log.debug("Temperature event detected! "+ event.values[0] + " degrees celcius (" + fahrenheit + " degrees farenheit");
			Log.d("SensorService", "Temperature event detected! "+ event.values[0] + " degrees celcius (" + fahrenheit + " degrees farenheit");
			mSensorManager.unregisterListener(SensorService.this, mTemperature);
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
