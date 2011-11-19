package com.sector67.space;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    public static final String SMS_EXTRA_NAME = "pdus";
    public static final String SMS_TRIGGER_TEXT = "Apollo67 Launch";

    public void onReceive(Context context, Intent intent ) {

        Bundle smsExtras = intent.getExtras();

        if ( null != smsExtras ) {
            // Get received SMS
            Object[] smsExtra = (Object[]) smsExtras.get( SMS_EXTRA_NAME );

            for ( int i = 0; i < smsExtra.length; ++i ) {


                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
                String body = sms.getMessageBody().toString();
                String address = sms.getOriginatingAddress();
                
                if(body.equalsIgnoreCase(SMS_TRIGGER_TEXT)){
                	Log.i(SMSReceiver.class.getName(), "SMS recieved from " + address + ", commencing launch");
                    final Intent spaceIntent = new Intent(context, SpaceActivity.class);
                    spaceIntent.addFlags(Intent.FLAG_FROM_BACKGROUND); 
                    spaceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                    context.startActivity(spaceIntent);
                }
            }

        }
    }
}
