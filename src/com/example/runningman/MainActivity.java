package com.example.runningman;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);    
        Button GoogleLogin = (Button) findViewById(R.id.buttonGoogleLogin);
        // generate dummy history records
        DBInterface DBI = new DBInterface(this);
        DBI.dummyHistory(true);
        dummyschedule();
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
				 Intent myIntent = new Intent(getApplicationContext(), NotifyService.class);
			        myIntent.putExtra("abc", "123456");
			        myIntent.setAction("com.example.runningman.NotifyAction");
			        AlarmManager alarms ;
			        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			        alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
			        alarms.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, alarmIntent);
			        Log.d("mainactivity", System.currentTimeMillis() + " ");
			}
        });
        
        

        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void dummyschedule()
    {	DBInterface DBI = new DBInterface(this);
    	ContentValues CV = new ContentValues();
    	CV.put("Date", "2013-11-17");
    	CV.put("Start", "09:00:00");
    	CV.put("End", "12:00:00");
    	CV.put("Status", "UPCOMING");
    	DBI.insert(DBI.tableSchedule, CV);
    	CV = new ContentValues();
    	CV.put("Date", "2013-11-15");
    	CV.put("Start", "08:00:00");
    	CV.put("End", "10:00:00");
    	DBI.insert(DBI.tableCalendar, CV);
    	CV = new ContentValues();
    	CV.put("Date", "2013-11-16");
    	CV.put("Start", "09:00:00");
    	CV.put("End", "12:00:00");
    	CV.put("Status", "UPCOMING");
    	DBI.insert(DBI.tableSchedule, CV);
    	CV = new ContentValues();
    	CV.put("Date", "2013-11-14");
    	CV.put("Start", "08:00:00");
    	CV.put("End", "10:00:00");
    	DBI.insert(DBI.tableCalendar, CV);
    }
    
}
