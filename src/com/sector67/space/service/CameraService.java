package com.sector67.space.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sector67.space.R;
import com.sector67.space.helper.DatabaseHelper;
import com.sector67.space.model.SensorActivity;

public class CameraService extends Activity implements SurfaceHolder.Callback {
	private Camera camera;
    private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	boolean isPreview;
    private final DatabaseHelper dbHelper = new DatabaseHelper(this);

	
	ShutterCallback shutterCallback = new ShutterCallback() {
		  public void onShutter() {
		    // TODO Do something when the shutter closes.
		  }
		};
		 
		PictureCallback jpegCallback = new PictureCallback() {
		  public void onPictureTaken(byte[] imageData, Camera _camera) {
			Format fileNameFormatter = new SimpleDateFormat("yyyyMMdd-kk-mm-ss-SSS");
			String fileName = fileNameFormatter.format(new Date());
			storeByteImage(CameraService.this, imageData, 100, fileName);
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("fileName", fileName);
			JSONObject dataObj = new JSONObject(dataMap);
			try {
				dbHelper.getSensorDao().create(new SensorActivity("Camera", new Date(), dataObj.toString()));
			} catch (SQLException e) {
    			Log.e(CameraService.class.getName(), "Unable to write to database", e);
			}
	        surfaceView.setVisibility(View.GONE);
		  }
		};

    public void onCreate(Bundle icircle) {
		super.onCreate(icircle);
		Log.d(CameraService.class.getName(), "Activity created");

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
    }
    
	protected void onResume() {
		super.onResume();


    }
    
    public void onDestroy() {
    	super.onDestroy();
    }
	
	public static boolean storeByteImage(Context mContext, byte[] imageData,
			int quality, String expName) {

        File sdImageMainDirectory = new File("/sdcard");
		FileOutputStream fileOutputStream = null;

		try {

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 5;
			
			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0,
					imageData.length,options);

			
			fileOutputStream = new FileOutputStream(
					sdImageMainDirectory.toString() +"/" + expName + ".jpg");
							
  
			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface size is changed so stop preview
        if (isPreview) {
                camera.stopPreview();
        }

        // Set camera properties to have correct preview size
        Camera.Parameters p = camera.getParameters();
        camera.setDisplayOrientation(CameraService.this.getWindowManager().getDefaultDisplay().getRotation());
        p.setPreviewSize(width, height);
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        p.setJpegQuality(100);
        camera.setParameters(p);

        try {
               camera.setPreviewDisplay(holder);
        } 
        catch (IOException e) {
                e.printStackTrace();
        }

        // Start preview again
        camera.startPreview();
        isPreview = true;
        camera.takePicture(shutterCallback, null, jpegCallback);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(); 
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
        isPreview = false;
        camera.release();
	}

}
