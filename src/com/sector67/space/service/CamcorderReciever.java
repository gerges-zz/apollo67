package com.sector67.space.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CamcorderReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		 Intent scheduledIntent = new Intent(context, CamcorderService.class);
		 Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
		 scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 context.startActivity(scheduledIntent);
	}

}