package com.sector67.space.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class CamcorderReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		 Bundle extras = intent.getExtras();
		 Intent scheduledIntent = new Intent(context, CamcorderService.class);
		 Toast.makeText(context, "Starting Video Capture", Toast.LENGTH_SHORT).show();
		 scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 scheduledIntent.putExtras(extras);
		 context.startActivity(scheduledIntent);
	}

}