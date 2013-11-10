package com.example.runningman;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {
	
	Button btn5, btn7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btn5 = (Button)findViewById(R.id.button5);
        btn7 = (Button)findViewById(R.id.button7);
        
        final Context myContext = this;
        
        btn5.setOnClickListener(new View.OnClickListener() {
        	
        	
        	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//
				Intent myIntent = new Intent(myContext, NotifyService.class);
				myIntent.putExtra("abc", "123456");
				myIntent.setAction("com.example.runningman.MainActivity");
				AlarmManager alarams ;
				PendingIntent alarmIntent = PendingIntent.getService(myContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				alarams = (AlarmManager) getSystemService(ALARM_SERVICE);
				alarams.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, alarmIntent);
				Log.d("mainactivity", System.currentTimeMillis() + " ");
			}

			
        	

		});
        
        
        btn7.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}

		});
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void Schedule(View view){
    	Intent intent = new Intent(this, Schedule.class);
    	startActivity(intent);
    }
    
    public void GetCurrentLocation(View view) {
    	//Intent intent = new Intent(this, GetMap.class);
    	//startActivity(intent);
    }
    
    public void PersonalData(View view) {
    	Intent intent = new Intent(this, PersonalData.class);
    	startActivity(intent);
    }
    public void Sensor(View view) {
    	Intent intent = new Intent(this, Sensor.class);
    	startActivity(intent);
    }
    
    public void History(View view) {
    	Intent intent = new Intent(this, History.class);
    	startActivity(intent);
    }
    
    
    
}
