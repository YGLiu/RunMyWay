package com.example.runningman;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements LocationListener{
	
	private LatLng currentLocation = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		if(location!=null)
		    onLocationChanged(location);
		locationManager.requestLocationUpdates(provider, 1000, 0, this);
        
        Button GoogleLogin = (Button) findViewById(R.id.buttonGoogleLogin);
        // generate dummy history records
        DBInterface DBI = new DBInterface(this);
        DBI.dummyHistory(true);
        GoogleLogin.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), GoogleLogin.class);
		    	startActivity(intent);

			}
        });
        Button OfflineLogin = (Button) findViewById(R.id.buttonOfflineLogin);
        OfflineLogin.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MainPage.class);
		    	startActivity(intent);
			}
        });
        
       
        Button notiTest = (Button) findViewById(R.id.buttonNotiTest);
        
        notiTest.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dummyschedule();
			}
        });
        detectEvents();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void dummyschedule()
    {	try
    	{	DBInterface DBI = new DBInterface(this);
	    	String date = "2013-11-15";
	    	String start = "10:50:00";
	    	String end = "12:00:00";
	    	ContentValues CV = new ContentValues();
	    	CV.put("Date", date);
	    	CV.put("Start", start);
	    	CV.put("End", end);
	    	CV.put("Status", "UPCOMING");
	    	DBI.insert(DBI.tableSchedule, CV);
	    	long time = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(date+start).getTime();
	    	Intent myIntent = new Intent(getApplicationContext(), NotifyService.class);
	        myIntent.putExtra("Message", "You have a message");
	        myIntent.setAction("com.example.runningman.NotifyAction");
	        AlarmManager alarms ;
	        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
	        alarms.set(AlarmManager.RTC, time-15*60*1000, alarmIntent);
	        Log.d("mainactivity", time-15*60*1000 + "");
	        Log.d("mainactivity", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(time-15*60*1000).toString());
    	}
    	catch(Exception e)
    	{	e.printStackTrace();}
    }
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
	}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void detectEvents()
	{	
		try
		{	DBInterface DBI = new DBInterface(this);
			Date cur = new Date();
			Cursor cursor = DBI.select("SELECT Date,Start FROM " + DBI.tableSchedule + " WHERE Status = 'UPCOMING'");
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{	Date sessionStart = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(0)+cursor.getString(1));
				long diff = sessionStart.getTime() - cur.getTime();
				if(diff <= 15*60*1000 && diff >= 0)
				{	Cursor userInfocsr = DBI.select("SELECT Latitude,Longtitude FROM " + DBI.tableUser);
					if(userInfocsr.moveToFirst())
					{	double lat = userInfocsr.getDouble(0);
						double lng = userInfocsr.getDouble(1);
						if(lat!=0 || lng != 0)
						{	LatLng home = new LatLng(lat,lng);
							if(currentLocation != null)
							{	float[] result = new float[3];
								Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, home.latitude, home.longitude, result);
								double distance = result[0];
								if(distance >= 1000)
								{
									AwayFromHome(sessionStart);
								}
								else
								{
									NormalNotification();
								}
							}	
							else
							{
								NormalNotification();
							}
						}
						else
						{
							NormalNotification();
						}
					}
					else
					{
						NormalNotification();
					}
				}
				cursor.moveToNext();
			}
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
	public void AwayFromHome(Date session)
	{	final Date sessionStart = session;
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
		// set title
		alertDialogBuilder.setTitle("Warning");
		// set dialog message and button events
		alertDialogBuilder
		.setMessage("You have a scheduled running session in 15 minutes. The system has detected that you are far from your home. Would you like to cancel your session?")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				DBInterface DBI = new DBInterface(getApplicationContext());
				String date = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(sessionStart);
				String start = new SimpleDateFormat("HH:mm:ss",Locale.US).format(sessionStart);
				DBI.delete(DBI.tableSchedule, "Date = '" + date + "' AND Start = '" + start + "'");
				dialog.cancel();
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				dialog.cancel();
			}
		});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create(); 
		// show alert dialog
		alertDialog.show();
	}
	public void NormalNotification()
	{	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
		// set title
		alertDialogBuilder.setTitle("Warning");
		// set dialog message and button events
		alertDialogBuilder
		.setMessage("You have a scheduled running session in 15 minutes")
		.setCancelable(false)
		.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				dialog.cancel();
			}
		});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create(); 
		// show alert dialog
		alertDialog.show();
		
	}
}