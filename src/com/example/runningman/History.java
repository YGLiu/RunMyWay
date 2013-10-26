package com.example.runningman;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class History extends Activity {
	private ArrayList<ContentValues> historyData = new ArrayList<ContentValues>();
	private DBInterface DBI;
	private int size;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		TabHost tabs=(TabHost)findViewById(android.R.id.tabhost); 
        tabs.setup(); 
        TabHost.TabSpec spec;
        
        spec=tabs.newTabSpec("tag1"); 
        spec.setContent(R.id.tab1); 
        spec.setIndicator("Distance"); 
        tabs.addTab(spec); 
        
        spec=tabs.newTabSpec("tag2"); 
        spec.setContent(R.id.tab2); 
        spec.setIndicator("Duration"); 
        tabs.addTab(spec); 
        
        spec=tabs.newTabSpec("tag3"); 
        spec.setContent(R.id.tab3); 
        spec.setIndicator("Calories Burnt"); 
        tabs.addTab(spec);
        
        spec=tabs.newTabSpec("tag4"); 
        spec.setContent(R.id.tab4); 
        spec.setIndicator("Average Speed"); 
        tabs.addTab(spec); 
        
        tabs.setCurrentTab(0); 
        
		DBI = new DBInterface(this);
		DisplayHistory();
		// Show the Up button in the action bar.
		setupActionBar();
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
		getMenuInflater().inflate(R.menu.history, menu);
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
	private void DisplayHistory()
	{	test();
		readDatabase();
		DistanceHistory();
		DurationHistory();
		CaloriesHistory();
		AveSpeedHistory();
	}
	private void readDatabase()
	{	historyData.clear();
		Cursor cursor = DBI.select("SELECT * FROM History");
		size = cursor.getCount();
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{	ContentValues CV = new ContentValues();
			CV.put("Date", cursor.getString(0));
			CV.put("Duration", cursor.getString(3));
			CV.put("Distance", cursor.getDouble(4));
			CV.put("AveSpeed", cursor.getDouble(5));
			historyData.add(CV);
			cursor.moveToNext();
		}
	}
	private void test()
	{	for(int i=0;i<3;i++)
		{	ContentValues CV = new ContentValues();
			CV.put("Date", "2013-10-0" + i);
			CV.put("Start", "start");
			CV.put("End", "end");
			CV.put("Duration", "abc");
			CV.put("Distance", i);
			CV.put("AveSpeed", 20);
			DBI.insert(DBI.tableHistory, CV);
		}
	}
	private void DistanceHistory()
	{	try
		{	GraphViewData[] GVD = new GraphViewData[size];
			long time;
			for(int i=0;i<size;i++)
			{	time = new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(historyData.get(i).getAsString("Date")).getTime();
				GVD[i] = new GraphViewData(time,historyData.get(i).getAsDouble("Distance"));
			}
			GraphViewSeries GVS = new GraphViewSeries(GVD);  
			GraphView GV = new LineGraphView(this,"Distance");
			GV.addSeries(GVS);
			LinearLayout layout = (LinearLayout) findViewById(R.id.tab1);
			layout.addView(GV);
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
	private void DurationHistory()
	{	try
		{	GraphViewData[] GVD = new GraphViewData[size];
			long time;
			for(int i=0;i<size;i++)
			{	time = new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(historyData.get(i).getAsString("Date")).getTime();
				GVD[i] = new GraphViewData(time,historyData.get(i).getAsDouble("Distance"));
			}
			GraphViewSeries GVS = new GraphViewSeries(GVD);  
			GraphView GV = new LineGraphView(this,"Duration");
			GV.addSeries(GVS);
			LinearLayout layout = (LinearLayout) findViewById(R.id.tab2);
			layout.addView(GV);
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
	private void CaloriesHistory()
	{	try
		{	GraphViewData[] GVD = new GraphViewData[size];
			long time;
			for(int i=0;i<size;i++)
			{	time = new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(historyData.get(i).getAsString("Date")).getTime();
				GVD[i] = new GraphViewData(time,historyData.get(i).getAsDouble("Distance"));
			}
			GraphViewSeries GVS = new GraphViewSeries(GVD);  
			GraphView GV = new LineGraphView(this,"Calories Burnt");
			GV.addSeries(GVS);
			LinearLayout layout = (LinearLayout) findViewById(R.id.tab3);
			layout.addView(GV);
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
	private void AveSpeedHistory()
	{	try
		{	GraphViewData[] GVD = new GraphViewData[size];
			long time;
			for(int i=0;i<size;i++)
			{	time = new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(historyData.get(i).getAsString("Date")).getTime();
				GVD[i] = new GraphViewData(time,historyData.get(i).getAsDouble("Distance"));
			}
			GraphViewSeries GVS = new GraphViewSeries(GVD);  
			GraphView GV = new LineGraphView(this,"Average Speed");
			GV.addSeries(GVS);
			LinearLayout layout = (LinearLayout) findViewById(R.id.tab4);
			layout.addView(GV);
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
}
