package com.example.runningman;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
