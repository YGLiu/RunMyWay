package com.example.runningman;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
public class NotifyService extends IntentService {

	public NotifyService() {
		super("NotifyService");
		// TODO Auto-generated constructor stub
	}


	@SuppressWarnings("deprecation")
	private void noti(Context myContext, String data) {
		NotificationManager mNotificationManager =
    	        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	// Sets an ID for the notification, so it can be updated
    	int notifyID = 1;
    	
    	Context context = getApplicationContext();
    	Intent notificationIntent = new Intent(context, MainPage.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    	//int myNumber = 1;
    	Notification.Builder mNotifyBuilder = new Notification.Builder(myContext)
    	    .setContentTitle("New Message")
    	    //.setVibrate(3000.00)
    	    .setWhen(System.currentTimeMillis())
    	    //.setNumber(++myNumber)
    	    .setDefaults(Notification.DEFAULT_SOUND)
    	    .setAutoCancel(true)
    	    .setContentText("You've received " + data + " messages.")
    	    .setContentIntent(contentIntent)
    	    .setSmallIcon(R.drawable.ic_launcher);
    	//int numMessages = 0;
    	// Start of a loop that processes data and then notifies the user
    	
    	// mNotifyBuilder.setContentText(currentText)
    	  //      .setNumber(++numMessages);
    	    // Because the ID remains unchanged, the existing notification is
    	    // updated.
    	    mNotificationManager.notify(
    	            notifyID,
    	            mNotifyBuilder.getNotification ());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String data = intent.getStringExtra("abc");
		noti(this, data);
	}
	

}
