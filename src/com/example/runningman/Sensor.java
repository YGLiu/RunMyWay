package com.example.runningman;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class Sensor extends Activity implements SensorListener,LocationListener{
	/** Called when the activity is first created. */
	private long mlCount = 0;
	private long mlTimerUnit = 100;
	private TextView tvTime;
	private Button btnStartPause;
	private Button btnStop;
	private Timer timer = null;
	private TimerTask task = null;
	private Handler handler = null;
	private Message msg = null;
	private boolean bIsRunningFlg = false;
	private static final String MYTIMER_TAG = "MYTIMER_LOG"; 
	
	// menu item
	private static final int SETTING_SECOND_ID = Menu.FIRST + 101;
	private static final int SETTING_100MILLISECOND_ID = Menu.FIRST + 102;
	
	// Setting timer unit flag
	private int settingTimerUnitFlg = SETTING_100MILLISECOND_ID;
	private int stepCount = 0;
	
	
	// Variable needed for map and database
	private GoogleMap map;
	private Polyline route = null;
	private ArrayList<LatLng> pos = new ArrayList<LatLng>();
	private DBInterface DBI;
	// End of variable needed for map and database
	private int seqnum = 0;
	
	SensorManager sm = null;
	
	TextView xViewA = null;
	TextView yViewA = null;
	TextView zViewA = null;
	TextView xViewO = null;
	TextView yViewO = null;
	TextView zViewO = null;
	TextView CheckStatusView = null;
	TextView stepView 	= null;
	TextView startView = null;
	TextView endView = null;
	
	int IsNotFirstRun = 0; 
	
	final String tag = "sensor_here";
	
	Time timeTemp=new Time("GMT+8");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		// Show the Up button in the action bar.
		setupActionBar();
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		xViewA = (TextView) findViewById(R.id.xbox);
        yViewA = (TextView) findViewById(R.id.ybox);
        zViewA = (TextView) findViewById(R.id.zbox);
        xViewO = (TextView) findViewById(R.id.xboxo);
        yViewO = (TextView) findViewById(R.id.yboxo);
        zViewO = (TextView) findViewById(R.id.zboxo);
        stepView = (TextView) findViewById(R.id.step);
        startView = (TextView) findViewById(R.id.startTime);
        endView = (TextView) findViewById(R.id.endTime);
        //CheckStatusView = (TextView) findViewById(R.id.check_status);
        
        
        tvTime = (TextView)findViewById(R.id.tvTime);
        btnStartPause = (Button)findViewById(R.id.btnStartPaunse);
        btnStop = (Button)findViewById(R.id.btnStop);
        
        SharedPreferences sharedPreferences = getSharedPreferences("mytimer_unit", Context.MODE_PRIVATE);
        //getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
        mlTimerUnit = sharedPreferences.getLong("time_unit", 100);
        Log.i(MYTIMER_TAG, "mlTimerUnit = " + mlTimerUnit);
		tvTime.setText(R.string.init_time_100millisecond);
		// Handle timer message
        handler = new Handler(){
    		@Override
    		public void handleMessage(Message msg) {
    			// TODO Auto-generated method stub
    			switch(msg.what) {
    			case 1:
    				mlCount++;
    				int totalSec = 0;
    				int yushu = 0;
    				
					totalSec = (int)(mlCount / 10);
        			yushu = (int)(mlCount % 10);
        			
    				// Set time display
    				//int hr  = (totalSec / 3600);
    				int hr  = 0;
    				int min = (totalSec / 60);
    				while (min >=60)
    				{
    					min-=60;
    					hr++;
    				}
    				while (hr >=24)
    				{
    					hr-=24;
    				}
    				
    				int sec = (totalSec % 60);
    				try{
    					tvTime.setText(String.format("%1$d:%2$02d:%3$02d.%4$d", hr,min, sec, yushu));
    					
    				} catch(Exception e) {
    					tvTime.setText("" + hr + ":" + min + ":" + sec + "." + yushu);
    					e.printStackTrace();
    					Log.e("MyTimer onCreate", "Format string error.");
    				}
    				break;
    				
    			default:
    				break;
    			}
    			super.handleMessage(msg);
    		}
    	};
    	
        btnStartPause.setOnClickListener(startPauseListener);
        btnStop.setOnClickListener(stopListener);
        
        TabHost tabs=(TabHost)findViewById(android.R.id.tabhost); 
        tabs.setup(); 
        TabHost.TabSpec spec=tabs.newTabSpec("tag1"); 
        spec.setContent(R.id.tab1); 
        spec.setIndicator("Timer"); 
        tabs.addTab(spec); 
        spec=tabs.newTabSpec("tag2"); 
        spec.setContent(R.id.tab2); 
        spec.setIndicator("Sensor"); 
        tabs.addTab(spec); 
        spec=tabs.newTabSpec("tag3"); 
        spec.setContent(R.id.tab3); 
        spec.setIndicator("Walking steps"); 
        tabs.addTab(spec);
        spec=tabs.newTabSpec("tag4"); 
        spec.setContent(R.id.tab4); 
        spec.setIndicator("Map"); 
        tabs.addTab(spec); 
        tabs.setCurrentTab(0); 
        //On create functions called for map and database
        DBI = new DBInterface(this);
        //test();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		if(location!=null)
		    onLocationChanged(location);
		locationManager.requestLocationUpdates(provider, 1000, 0, this);
		
		//End of On create functions called for map and database

	}
	 public void onSensorChanged(int sensor, float[] values) {
	        synchronized (this) {
	            Log.d(tag, "onSensorChanged: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
	            if (sensor == SensorManager.SENSOR_ORIENTATION) {
		            xViewO.setText("Orientation X: " + values[0]);
		            yViewO.setText("Orientation Y: " + values[1]);
		            zViewO.setText("Orientation Z: " + values[2]);
		            
	            }
	            if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
		            xViewA.setText("Accel X: " + values[0]);
		            yViewA.setText("Accel Y: " + values[1]);
		            zViewA.setText("Accel Z: " + values[2]);
		            if (values[2] < -11 || values[2] >11)
		            {
		            	stepCount++;
		            }
		            // (values[2] >-9 & values[2] < 9 ) |
		            /*if(values[0]>3 || values[1]>3 || values[0]<-3 || values[1]<-3)
		            {
		            	CheckStatusView.setBackgroundColor(0x0000FF00);
		            	CheckStatusView.setText("In train");
		            }
		            else 
		            {
		            	CheckStatusView.setBackgroundColor(0x00FF0000);
		            	CheckStatusView.setText("In station");
		            }*/
	            }          
	            stepView.setText("Number: " + stepCount);
	        }
	    }
	    
	    public void onAccuracyChanged(int sensor, int accuracy) {
	    	Log.d(tag,"onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
	        
	    }
	 

	    @Override
	    protected void onResume() {
	        super.onResume();
	        sm.registerListener(this, 
	                SensorManager.SENSOR_ORIENTATION |
	        		SensorManager.SENSOR_ACCELEROMETER,
	                SensorManager.SENSOR_DELAY_NORMAL);
	    }
	    
	    @Override
	    protected void onStop() {
	        sm.unregisterListener(this);
	        super.onStop();
	    } 
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor, menu);
		//-------------------------

		super.onCreateOptionsMenu(menu);
		
		Log.i(MYTIMER_TAG, "Menu is created.");
		
		// Stop timer
		if (null != task) {
			task.cancel();
			task = null;
		}
		if (null != timer) {
			timer.cancel(); // Cancel timer
			timer.purge();
			timer = null;
			handler.removeMessages(msg.what);
		}
		
		bIsRunningFlg = false;
		mlCount = 0;

		btnStartPause.setText("Start");
		//btnStartPause.setImageResource(R.drawable.start);
				
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    // Start and pause
    View.OnClickListener startPauseListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i(MYTIMER_TAG, "Start/Pause is clicked.");
			
			if(IsNotFirstRun == 0)
			{
				//flag you need 
				IsNotFirstRun = 1;
				
				SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd    HH:mm:ss     ");       
		        Date    curDate    =   new    Date(System.currentTimeMillis());//获取当前时间       
		        String    str1    =    formatter.format(curDate); 
		        startView.setText("Start time："+ str1); 
		        endView.setText(""); 
		        // you can implement below
				DBI.delete(DBI.tableSession, null);
				seqnum = 0;
			}
			
			
			if (null == timer) {
				if (null == task) {
					task = new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (null == msg) {
								msg = new Message();
							} else {
								msg = Message.obtain();
							}
							msg.what = 1;
							handler.sendMessage(msg);
						}
						
					};
				}
				timer = new Timer(true);
				timer.schedule(task, mlTimerUnit, mlTimerUnit); // set timer duration
			}
			
			// start
			if (!bIsRunningFlg) {
				bIsRunningFlg = true;
				
				
		        
				//btnStartPause.setImageResource(R.drawable.pause);
				btnStartPause.setText("pause");
			} else { // pause
				try{
					bIsRunningFlg = false;
					task.cancel();
					task = null;
					timer.cancel(); // Cancel timer
					timer.purge();
					timer = null;
					handler.removeMessages(msg.what);
					btnStartPause.setText("Start");
					//btnStartPause.setImageResource(R.drawable.start);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
    };
    
    // Stop
    View.OnClickListener stopListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i(MYTIMER_TAG, "Stop is clicked.");

			if(IsNotFirstRun == 1)
			{
				//flag you need 
				IsNotFirstRun = 0;
				
				SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd    HH:mm:ss     ");       
		        Date    curDate    =   new    Date(System.currentTimeMillis());//获取当前时间       
		        String    str2    =    formatter.format(curDate); 
		        endView.setText("End time："+ str2); 
		        
		        //you can implement below
		        
				
			}
			
			if (null != timer) {
								
				task.cancel();
				task = null;
				timer.cancel(); // Cancel timer
				timer.purge();
				timer = null;
				handler.removeMessages(msg.what);
			}
			
			mlCount = 0;
			bIsRunningFlg = false;

			btnStartPause.setText("Start");
			//btnStartPause.setImageResource(R.drawable.start);
			tvTime.setText(R.string.init_time_100millisecond);
			
		}
    	
    };
    
    
  
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (KeyEvent.KEYCODE_MENU == keyCode) {
			super.openOptionsMenu();  // 调用这个，就可以弹出菜单

			Log.i(MYTIMER_TAG, "Menu key is clicked.");
				
			// Stop timer
			if (null != task) {
				task.cancel();
				task = null;
			}
			if (null != timer) {
				timer.cancel(); // Cancel timer
				timer.purge();
				timer = null;
				handler.removeMessages(msg.what);
			}
			
			bIsRunningFlg = false;
			mlCount = 0;
			btnStartPause.setText("Start");
			//btnStartPause.setImageResource(R.drawable.start);
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	//Functions for map
	@Override
    public void onLocationChanged(Location location) {
		LatLng CL;
		CL = new LatLng(location.getLatitude(), location.getLongitude());
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CL, 17));
		if(IsNotFirstRun == 1)
		{	ContentValues CV = new ContentValues();
			CV.put("SeqNum", seqnum);
			CV.put("Lat", CL.latitude);
			CV.put("Lng", CL.longitude);
			DBI.insert(DBI.tableSession, CV);
			seqnum++;
			showRoute();
		}
    }
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	private void showRoute()
	{	if(route != null)
			route.remove();
		readRouteData();
		route = map.addPolyline(new PolylineOptions().addAll(pos).width(5).color(0xFFFF0000));
	}
	private void readRouteData()
	{	pos.clear();
		Cursor cursor = DBI.select("SELECT * FROM SESSION ORDER BY SeqNum ASC");
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{	pos.add(new LatLng(cursor.getDouble(1),cursor.getDouble(2)));
			cursor.moveToNext();
		}
	}
	
	//End of functions for map
    }

