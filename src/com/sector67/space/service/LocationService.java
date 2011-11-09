package com.sector67.space.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class LocationService extends Service{
    private Handler mHandler = new Handler(Looper.getMainLooper());
    
    private final IBinder mBinder = new Binder() {
        @Override
                protected boolean onTransact(int code, Parcel data, Parcel reply,
                        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
    
    Runnable mTask = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 15*1000;

	 	   // Acquire a reference to the system Location Manager
	        final LocationManager locationManager = (LocationManager) LocationService.this.getSystemService(Context.LOCATION_SERVICE);
	        
	        // Define a listener that responds to location updates
	        final LocationListener locationListener = new LocationListener() {
	            public void onLocationChanged(Location location) {
	            	// Called when a new location is found by the network location provider.
	            	//log.debug("Location found! " + location);
	            	Log.d("LocationService", "Location found! " + location);
	            	locationManager.removeUpdates(this);
	            }
	
	            public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	            public void onProviderEnabled(String provider) {}
	
	            public void onProviderDisabled(String provider) {}
	          };
	
	        // Register the listener with the Location Manager to receive location updates
	       mHandler.post(new Runnable() {
	              public void run() {
	            	  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	              }
	        });
           
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
	       LocationService.this.stopSelf();
        }
    };
    
    
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
    public void onCreate() {
        Thread thr = new Thread(null, mTask, "LocationService");
        thr.start();
    }
    
    
    public void onDestroy() {
    	
    }
}
