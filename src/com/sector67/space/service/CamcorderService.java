package com.sector67.space.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.sector67.space.R;
import com.sector67.space.helper.DatabaseHelper;
import com.sector67.space.model.SensorActivity;

public class CamcorderService extends Activity implements SurfaceHolder.Callback {
	private MediaRecorder mRecorder;
    private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private boolean recording;
	private static final int TIME_TO_RECORD =  30000;

    private final DatabaseHelper dbHelper = new DatabaseHelper(this);
    private final IBinder mBinder = new Binder() {
        @Override
                protected boolean onTransact(int code, Parcel data, Parcel reply,
                        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
	
    public void onCreate(Bundle icircle) {
		super.onCreate(icircle);
		Log.d(CamcorderService.class.getName(), "Activity created");

        // Configure window
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Use camera.xml as content view
        setContentView(R.layout.camera);

        // Get surface view and initialize surface holder
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mRecorder = new MediaRecorder();
    }
    
	protected void onResume() {
		super.onResume();


    }
    
    public void onDestroy() {
    	super.onDestroy();
    }

    
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Format fileNameFormatter = new SimpleDateFormat("yyyyMMdd-kk-mm-ss-SSS");
		String fileName = fileNameFormatter.format(new Date());
		initRecorder(fileName);
        try {
            long endTime = System.currentTimeMillis() + TIME_TO_RECORD;
        	mRecorder.setPreviewDisplay(surfaceHolder.getSurface());
			mRecorder.prepare();
	        mRecorder.start();
	        recording = true;
	        Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("fileName", fileName);
			JSONObject dataObj = new JSONObject(dataMap);
           while (System.currentTimeMillis() < endTime) {
               synchronized (mBinder) {
                   try {
                       mBinder.wait(endTime - System.currentTimeMillis());
                   } catch (Exception e) {
                   }
               }
           }
			if(recording) {
				mRecorder.stop();
				mRecorder.reset();
			}
			try {
				dbHelper.getSensorDao().create(new SensorActivity("Camecorder", new Date(), dataObj.toString()));
			} catch (SQLException e) {
    			Log.e(CameraService.class.getName(), "Unable to write to database", e);
			}
		} catch (IllegalStateException e) {
			Log.e(CamcorderService.class.getName(), "Unable to start recording", e);
		} catch (IOException e) {
			Log.e(CamcorderService.class.getName(), "Unable to start recording", e);
		}
	}

	private void initRecorder(String fileName) {
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

	    CamcorderProfile cpHigh = CamcorderProfile
	            .get(CamcorderProfile.QUALITY_HIGH);
	    mRecorder.setProfile(cpHigh);
	    mRecorder.setOutputFile("/sdcard/"+fileName+".mp4");
	    mRecorder.setMaxDuration(50000); // 50 seconds
	    mRecorder.setMaxFileSize(5000000); // Approximately 5 megabytes
	}

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mRecorder.release();
	}

}
