package com.sector67.space.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
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
	private static final int TIME_TO_RECORD =  300000;

    private final DatabaseHelper dbHelper = new DatabaseHelper(this);

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

	}

	private void initRecorder(String fileName) {
		String videoDir = "/sdcard/apollo67/videos/";
		File sdImageMainFile = new File(videoDir);
        if(!sdImageMainFile.exists() && !sdImageMainFile.mkdirs()) {
                Log.e(CameraService.class.getName(), "Path to file could not be created.");
        }
        mRecorder.setOutputFile(videoDir + fileName + ".mp4");
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

	    CamcorderProfile cpHigh = CamcorderProfile
	            .get(CamcorderProfile.QUALITY_HIGH);
	    mRecorder.setProfile(cpHigh);
	    mRecorder.setMaxDuration(TIME_TO_RECORD);
	    mRecorder.setMaxFileSize(100000000);
	}

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Format fileNameFormatter = new SimpleDateFormat("yyyyMMdd-kk-mm-ss-SSS");
		String fileName = fileNameFormatter.format(new Date());
		initRecorder(fileName);
        try {
        	mRecorder.setPreviewDisplay(surfaceHolder.getSurface());
			mRecorder.prepare();
	        mRecorder.start();
	        Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("fileName", fileName);
			JSONObject dataObj = new JSONObject(dataMap);
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
        
        Timer timer = new Timer();

        timer.schedule( new TimerTask(){
           public void run() { 
               CamcorderService.this.finish();
            }
         }, TIME_TO_RECORD);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mRecorder.release();
	}

}
