package com.example.runningman;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GCMBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		if(action.equals("com.google.android.c2dm.intent.REGISTRATION")){
			String registrationId = intent.getStringExtra("registration_id");
			Log.i("uo", registrationId);
			String error = intent.getStringExtra("error");
			//Log.i("uoe", error);
			String unregistered = intent.getStringExtra("unregistered");
		
		}else if(action.equals("com.google.android.c2dm.intent.RECEIVE")){
			String data1 = intent.getStringExtra("data1");
			String data2 = intent.getStringExtra("data2");
			
			Log.i("uoo", data1);
			
					
		}
	}
	

	
}
