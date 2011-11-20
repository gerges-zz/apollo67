package com.sector67.space.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

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

import com.sector67.space.helper.DatabaseHelper;
import com.sector67.space.model.SensorActivity;

public class LocationService extends Service{
	public static final String LOCATION_UPDATE = "com.sector67.space.service.LocationService.action.LOCATION_UPATE";
	public static final String LONGITUDE = "com.sector67.space.service.LocationService.action.LONGITUDE";
	public static final String LATTITUDE = "com.sector67.space.service.LocationService.action.LATTITUDE";
	public static final String ALTITUDE = "com.sector67.space.service.LocationService.action.ALTITUDE";
	public static final String SERVER_URL = "http://apollo67.com/service.php";

	
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final DatabaseHelper dbHelper = new DatabaseHelper(this);

    
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
	        		try {
	        			JSONObject dataObj = convertLocationToJSON(location);
	        			announceLocationChanges(location.getLatitude(), location.getLongitude(), location.getAltitude());
	        			postToWeb(SERVER_URL, dataObj);
	        			dbHelper.getSensorDao().create(new SensorActivity("Location", new Date(), dataObj.toString()));
	        		} catch (SQLException e) {
	        			Log.e(LocationService.class.getName(), "Unable to write to database", e);
	        		}
	            	Log.d(LocationService.class.getName(), "Location found! " + location);
	            	locationManager.removeUpdates(this);
	            }

				private JSONObject convertLocationToJSON(Location location) {
					Map<String, String> dataMap = new HashMap<String, String>();
					dataMap.put("lattitude", Double.toString(location.getLatitude()));
					dataMap.put("longitude", Double.toString(location.getLongitude()));
					dataMap.put("altitude", Double.toString(location.getAltitude()));
					dataMap.put("accuracy", Double.toString(location.getAccuracy()));
					JSONObject dataObj = new JSONObject(dataMap);
					return dataObj;
				}
				
				private void postToWeb(String serverUrl, JSONObject location) {
					int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
					HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
					HttpClient client = new DefaultHttpClient(httpParams);
					
					HttpPost request = new HttpPost(serverUrl);
					try {
							Log.d("TEST", location.toString());
							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
							nameValuePairs.add(new BasicNameValuePair("data", location.toString()));
							request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
							client.execute(request);
					} catch (Exception e) { // I know, it's late
						Log.e(LocationService.class.getName(), "Unable to post to service", e);
					}
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
    	super.onCreate();
        Thread thr = new Thread(null, mTask, "LocationService");
        thr.start();
    }
    
    
    public void onDestroy() {
    	super.onDestroy();
    }
    
    private void announceLocationChanges(double lattitude, double longitude, double altitude) {
        Intent intent = new Intent(LOCATION_UPDATE);
        intent.putExtra(LONGITUDE, lattitude);
        intent.putExtra(LATTITUDE, longitude);
        intent.putExtra(ALTITUDE, altitude);
        sendBroadcast(intent);
    }
}
