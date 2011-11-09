package com.sector67.space.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
import android.view.Window;
import android.view.WindowManager;

import com.sector67.space.R;

public class CameraService extends Activity {
	Camera mCamera;
	SurfaceHolder mSurfaceHolder;
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		  public void onShutter() {
		    // TODO Do something when the shutter closes.
		  }
		};
		 
		PictureCallback rawCallback = new PictureCallback() {
		  public void onPictureTaken(byte[] _data, Camera _camera) {
		    // TODO Do something with the image RAW data.
		  }
		};
		 
		PictureCallback jpegCallback = new PictureCallback() {
		  public void onPictureTaken(byte[] imageData, Camera _camera) {
			storeByteImage(CameraService.this, imageData, 50, "ImageName");
		  }
		};

    public void onCreate(Bundle icircle) {
		super.onCreate(icircle);

		Log.e("CameraService", "onCreate");

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera);
		SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		//StoreByteImage(CameraService.this, imageData, 50, "ImageName");
		//mCamera.startPreview();
    }
    
	protected void onResume() {
		super.onResume();
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
			mCamera.stopPreview();
			mCamera.release();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    public void onDestroy() {
    	
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
					sdImageMainDirectory.toString() +"/image.jpg");
							
  
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
}
