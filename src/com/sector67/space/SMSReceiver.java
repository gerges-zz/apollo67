package com.sector67.space;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	private SharedPreferences sharedPreferences;
    public static final String SMS_EXTRA_NAME = "pdus";

    public void onReceive(Context context, Intent intent ) {
        if(null == sharedPreferences) {
        	sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(context);
        }
        String triggerText = sharedPreferences.getString("trigger", "Apollo67 Launch");


        Bundle smsExtras = intent.getExtras();

        if ( null != smsExtras ) {
            // Get received SMS
            Object[] smsExtra = (Object[]) smsExtras.get( SMS_EXTRA_NAME );

            for ( int i = 0; i < smsExtra.length; ++i ) {


                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
                String body = sms.getMessageBody().toString();
                String address = sms.getOriginatingAddress();
                
                if(body.equalsIgnoreCase(triggerText)){
                	Log.i(SMSReceiver.class.getName(), "SMS recieved from " + address + ", commencing launch");
                    final Intent launchIntent = new Intent(context, LaunchActivity.class);
                    launchIntent.addFlags(Intent.FLAG_FROM_BACKGROUND); 
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                    context.startActivity(launchIntent);
                }
            }

        }
    }
}
