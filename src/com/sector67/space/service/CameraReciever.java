package com.sector67.space.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class CameraReciever extends BroadcastReceiver {
	public static final String CAMERA_STOP = "com.sector67.space.service.CameraService.action.STOP";


	@Override
	public void onReceive(Context context, Intent intent) {
		 Bundle extras = intent.getExtras();
		 String action = extras.getString("action");
		 if(null == action || !action.equals("stop")) {
			 Intent scheduledIntent = new Intent(context, CameraService.class);
			 Toast.makeText(context, "String Photo Capture", Toast.LENGTH_SHORT).show();
			 scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 context.startActivity(scheduledIntent);
		 } else {
			 Intent stopIntent = new Intent(CAMERA_STOP);
			 context.sendBroadcast(stopIntent);
		 }
	}

}