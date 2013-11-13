package com.example.runningman;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.weather.tools.NetworkUtils;

public class Schedule extends Activity {
	private DBInterface DBI;
	private double AveDuration;
	private int count_Mon;
	private int count_Tue;
	private int count_Wed;
	private int count_Thu;
	private int count_Fri;
	private int count_Sat;
	private int count_Sun;
	private int count_morning;
	private int count_afternoon;
	private int count_evening;
	private int count_midnight;
	private int num_days;
	private Weather weather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		DBI = new DBInterface(this);
		DBI.verboseTable(DBI.tableSchedule);
		// check network availability
		if (!NetworkUtils.isConnected(getApplicationContext())) {
        	Toast.makeText(getApplicationContext(), "Network connection is unavailable!!", Toast.LENGTH_SHORT).show();
        	return;
        }
		
		// debugging purpose
		DBI.verboseTable(DBI.tableCalendar);
		Button rebuildConflict = (Button) findViewById(R.id.conflict);
		Button rebuildMissed = (Button) findViewById(R.id.missed);
		rebuildConflict.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				 RecomputeConflict();
			}
        });
		rebuildMissed.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				 RecomputeMissed();
			}
        });
		// initiate Weather table
		weather = new Weather(getApplicationContext());
		getHistoryPattern();
		updateSchedule();
		ComputeSchedule();
		displaySchedule();
	}
	
	public void ComputeSchedule()
	{	if(IsScheduleEmpty())
		{	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
		// set title
		alertDialogBuilder.setTitle("Warning");
		// set dialog message and button events
		alertDialogBuilder
		.setMessage("Currently you do not have any schedule.\nWould you like to build one?")
		.setCancelable(false)
		.setPositiveButton("Build",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				Compute();
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
		else
		{
			if(gotConflictEvents())
			{	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
				// set title
				alertDialogBuilder.setTitle("Warning");
				// set dialog message and button events
				alertDialogBuilder
				.setMessage("You have conflicts in your schedule.\nWould you like to re-build?")
				.setCancelable(false)
				.setPositiveButton("Build",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						RecomputeConflict();
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
			if(gotMissedEvents())
			{	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
				// set title
				alertDialogBuilder.setTitle("Warning");
				// set dialog message and button events
				alertDialogBuilder
				.setMessage("You have missed sessions in your schedule.\nWould you like to re-build?")
				.setCancelable(false)
				.setPositiveButton("Build",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						RecomputeMissed();
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
	}
	public void Compute()
	{	Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableHistory);
		cursor.moveToFirst();
		cursor.close();
		ArrayList<slot> emptyslots = new ArrayList<slot>();
		Date date = new Date();
		cursor = DBI.select("SELECT * FROM " + DBI.tableCalendar);
		
		if(cursor.getInt(0) > 30)
		{	
			
		}
		else
		{
			
		}
		
	}
	public void RecomputeConflict()
	{}
	public void RecomputeMissed()
	{}
	public boolean gotConflictEvents()
	{	Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableSchedule + " WHERE Status = 'CONFLICT'");
		cursor.moveToFirst();
		if(cursor.getInt(0) > 0)
			return true;
		else
			return false;
	}
	public boolean gotMissedEvents()
	{	Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableSchedule + " WHERE Status = 'MISSED'");
		cursor.moveToFirst();
		if(cursor.getInt(0) > 0)
			return true;
		else
			return false;
	}
	public boolean IsScheduleEmpty()
	{	Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableSchedule);
		cursor.moveToFirst();
		if(cursor.getInt(0) == 0)
		{	cursor.close();
			return true;
		}
		else
		{	cursor.close();
			return false;
		}
	}
	public void updateSchedule()
	{	try	
		{	Cursor cursor = DBI.select("SELECT * FROM " + DBI.tableSchedule + " WHERE Status <> 'PASSED'");
			Date curr = new Date();
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{	if((new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(1) + cursor.getString(2))).after(curr))
				{	ContentValues CV = new ContentValues();
					if(isConflict(cursor.getInt(0)))
					{	CV.put("Status", "CONFLICT");
						DBI.update(DBI.tableSchedule, CV, "Id = '" + cursor.getInt(0) + "'");
					}
					else
					{	CV.put("Status", "UPCOMING");
						DBI.update(DBI.tableSchedule, CV, "Id = '" + cursor.getInt(0) + "'");
					}
				}
				else
				{	ContentValues CV = new ContentValues();
					if(IsDone(cursor.getInt(0)))
					{	CV.put("Status", "PASSED");
						DBI.update(DBI.tableSchedule, CV, "Id = '" + cursor.getInt(0) + "'");
					}
					else
					{	CV.put("Status", "MISSED");
						DBI.update(DBI.tableSchedule, CV, "Id = '" + cursor.getInt(0) + "'");
					}
				}
				cursor.moveToNext();
			}
			cursor.close();
			cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableSchedule + " WHERE Status <> 'PASSED'");
			cursor.moveToFirst();
			if(cursor.getInt(0) == 0)
				DBI.delete(DBI.tableSession, null);
			cursor.close();
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
	
	// tell if a schedule ID has conflict with Schedule and Weather
	public boolean isConflict(int ID)
	{
		Cursor scheduleCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule + " WHERE ID='" +
				Integer.toString(ID) + "'");
		
		// if the Schedule entry is found
		if (scheduleCursor.moveToFirst()) 
		{
			String scheduleDate = scheduleCursor.getString(1);				
			// select events from calendar which have the same date
			String calQuery = "SELECT * FROM " + DBI.tableCalendar + " WHERE Date='" + scheduleDate + "'";			
			Cursor calendarCursor = DBI.select(calQuery);
			
			String wthQuery = "SELECT * FROM " + DBI.tableWeather + " WHERE Date='" + scheduleDate + "'";
			Cursor weatherCursor = DBI.select(wthQuery);
			
			// check conflicts with calendar
			if (calendarCursor.moveToFirst()) 
			{
				while(!calendarCursor.isAfterLast()) 
				{					
					String calStart = calendarCursor.getString(1);
					String schStart = scheduleCursor.getString(2);
					String calEnd = calendarCursor.getString(2);
					String schEnd = scheduleCursor.getString(3);
					
					try {
						SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss", Locale.US);
						// conversion to Date type
						Date calStartDate = parser.parse(calStart);
						Date calEndDate = parser.parse(calEnd);
						Date schStartDate = parser.parse(schStart);
						Date schEndDate = parser.parse(schEnd);
						// if event slot has overlapping part
						if ((calStartDate.before(schEndDate) && calStartDate.after(schStartDate)) ||
								calStartDate.equals(schStartDate) || calEndDate.equals(schEndDate) || 
								(calEndDate.before(schEndDate) && calEndDate.after(schStartDate)) ||
								(calStartDate.before(schStartDate) && calEndDate.after(schEndDate))) {
							return true;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					calendarCursor.moveToNext();
				}
			}
			
			// check conflicts with weather
			if (weatherCursor.moveToFirst()) {
				// one day has only one weather record
				String weatherText = weatherCursor.getString(1);
				// if poor weather condition
				if (weather.isWeatherPoorCondition(weatherText)) {
					return true;
				}
			}
		}
		return false;
	}
	public boolean IsDone(int ID)
	{
		Cursor cursor = DBI.select("SELECT * FROM " + DBI.tableSchedule + " WHERE Id = '" + Integer.toString(ID) + "'");
		cursor.moveToFirst();
		String date = cursor.getString(1);
		String start = cursor.getString(2);
		String end = cursor.getString(3);
		String historyStart, historyEnd;
		try{
			SimpleDateFormat parser = new SimpleDateFormat ("HH:mm:ss", Locale.US);
			Date calStart = parser.parse(start);
			Date calEnd = parser.parse(end);
			cursor = DBI.select("SELECT * FROM " + DBI.tableHistory + " WHERE Date = '" + date + "'");
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{	historyStart = cursor.getString(1);
				historyEnd = cursor.getString(2);
				Date calHistoryStart = parser.parse(historyStart);
				Date calHistoryEnd = parser.parse(historyEnd);
				//compare
				if (calStart.before(calHistoryEnd) && calHistoryStart.before(calStart))
					return true;
				else if (calEnd.before(calHistoryEnd) && calHistoryStart.before(calEnd))
					return true;
				else if (calStart.before(calHistoryStart) && calHistoryEnd.before(calEnd))
					return true;
				else if (calStart.equals(calHistoryStart) || calHistoryEnd.equals(calEnd))
					return true;
				else
					cursor.moveToNext();
			}
			return false;
		}
		catch (ParseException ex) 
		{ 	ex.printStackTrace();}
		return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	// return an ArrayList of conflicting Id if they collide
	public ArrayList<String> isScheduleCollidesCalendar() {
		ArrayList<String> coflictIdList = new ArrayList<String>();
		Cursor scheduleCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		
		// if exist at least one event
		if (scheduleCursor.moveToFirst()) {	
			while(!scheduleCursor.isAfterLast()) {
				String scheduleDate = scheduleCursor.getString(1);				
				// select events from calendar which have the same date
				String query = "SELECT * FROM " + DBI.tableCalendar + " WHERE Date='" + scheduleDate + "'";			
				Cursor calendarCursor = DBI.select(query);
				
				if (calendarCursor.moveToFirst()) {
					while(!calendarCursor.isAfterLast()) {
						// conversion to Date type
						String schId = Integer.toString((scheduleCursor.getInt(0)));
						String calStart = calendarCursor.getString(2);
						String schStart = scheduleCursor.getString(2);
						String calEnd = calendarCursor.getString(3);
						String schEnd = scheduleCursor.getString(3);
						
						try {
							SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss", Locale.US);
							Date calStartDate = parser.parse(calStart);
							Date calEndDate = parser.parse(calEnd);
							Date schStartDate = parser.parse(schStart);
							Date schEndDate = parser.parse(schEnd);
							// if event slot has overlapping part
							if ((calStartDate.before(schEndDate) && calStartDate.after(schStartDate)) ||
									calStartDate.equals(schStartDate) || calStartDate.equals(schEndDate) ||
									(calEndDate.before(schEndDate) && calEndDate.after(schStartDate)) ||
									calEndDate.equals(schStartDate) || calEndDate.equals(schEndDate)) {
								// add an Id to the ArrayList
								coflictIdList.add(schId);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						calendarCursor.moveToNext();
					}
				}				
				scheduleCursor.moveToNext();
			}
		}
		return coflictIdList;
	}
	
	public ArrayList<String> isScheduleCollidesWeather() {
		ArrayList<String> conflictIdList = new ArrayList<String>();
		Cursor scheduleCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		String weatherText;
		// if exist at least one event
		if (scheduleCursor.moveToFirst()) {
			while(!scheduleCursor.isAfterLast()) {
				String schId = Integer.toString(scheduleCursor.getInt(0));
				String scheduleDate = scheduleCursor.getString(1);				
				// select events from Weather which have the same date
				String query = "SELECT * FROM " + DBI.tableWeather + " WHERE Date='" + scheduleDate + "'";			
				Cursor weatherCursor = DBI.select(query);
				if(weatherCursor.moveToFirst())
				{	weatherText = weatherCursor.getString(1);
					// if poor weather condition, push the Id into the list
					if (weather.isWeatherPoorCondition(weatherText))
						conflictIdList.add(schId);
				}
				scheduleCursor.moveToNext();
			}
		}
		return conflictIdList;
	}
	
	// return # hours of history behind planned schedule
	public double isProgressBehindSchedule() {
		double hisSum = 0, schSum = 0;
		double hrsBehind = 0;
		Cursor schCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		Cursor hisCursor = DBI.select("SELECT * FROM " + DBI.tableHistory);
		Date currDate = new Date();
		SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss", Locale.US);
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		
		// calculate scheduled exercise duration before today
		if (schCursor.moveToFirst()) {
			while(!schCursor.isAfterLast()) {
				try {				
					String schDate = schCursor.getString(1);
					String schStart = schCursor.getString(2);
					String schEnd = schCursor.getString(3);
					Date schDateDate = dateParser.parse(schDate);
					Date schStartDate = timeParser.parse(schStart);
					Date schEndDate = timeParser.parse(schEnd);
					
					// if the dates from Schedule is before today
					if (schDateDate.before(currDate)) {
						long diff = schEndDate.getTime() - schStartDate.getTime();
						long diffHours = diff / (60 * 60 * 1000) % 24;
						schSum += ((double) diffHours);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				schCursor.moveToNext();
			}
		}		
		// calculate the actual exercise duration before today
		if (hisCursor.moveToFirst()) {
			while(!hisCursor.isAfterLast()) {
				try {
					String hisDate = hisCursor.getString(0);
					double duration = hisCursor.getDouble(3);
					Date hisDateDate = dateParser.parse(hisDate);
					
					// if dates from History is before today
					if (hisDateDate.before(currDate)) {
						hisSum += duration;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				hisCursor.moveToNext();
			}
		}
		hrsBehind = schSum - hisSum;
		return hrsBehind;
	}
	
	private void displaySchedule() {
		List<String> listValues = new ArrayList<String>();
		String query = "SELECT * FROM " + DBI.tableSchedule + " ORDER BY Date ASC";
		Cursor cursor = DBI.select(query);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) 
		{	String listEntry = cursor.getString(1) + "  " + cursor.getString(2) + " - " + cursor.getString(3) + "  " + cursor.getString(4);
			listValues.add(listEntry);
			cursor.moveToNext();
		}
		cursor.close();
		// display on the listView
		ListView listview = (ListView) findViewById(R.id.listViewSchedule);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listValues);
		listview.setAdapter(adapter);
	}

	private void getHistoryPattern()
	{	try
		{	ArrayList<HistoryData> historyData = new ArrayList<HistoryData>();
			Cursor cursor = DBI.select("SELECT Date,Duration,Start FROM " + DBI.tableHistory);
			cursor.moveToFirst();
			AveDuration = 0;
			double sum = 0;
			int count = 0;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -30);
			Date a_month_ago = cal.getTime();
			count_Mon = count_Tue = count_Wed = count_Thu = count_Fri = count_Sat = count_Sun = 0;
			count_morning = count_afternoon = count_evening = count_midnight = 0;
			while(!cursor.isAfterLast())
			{	HistoryData history = new HistoryData(cursor.getString(0),cursor.getString(2),"",cursor.getDouble(1),0,0); 
				historyData.add(history);
				cursor.moveToNext();
			}
			cursor.close();
			for(HistoryData data : historyData)
			{	sum += data.duration;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Mon"))
					count_Mon++;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Tue"))
					count_Tue++;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Wed"))
					count_Wed++;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Thu"))
					count_Thu++;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Fri"))
					count_Fri++;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Sat"))
					count_Sat++;
				if(new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).equals("Sun"))
					count_Sun++;
				if((new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(data.date)).after(a_month_ago))
					count++;
				if(0 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() < 6)
					count_midnight++;
				if(6 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() < 12)
					count_morning++;
				if(12 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() < 18)
					count_afternoon++;
				if(18 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(data.start).getHours() <= 23)
					count_evening++;
			}
			AveDuration = sum / historyData.size();
			if(count != 0)
				num_days = 30 / count;
		}
		catch(Exception e)
		{	e.printStackTrace(); }
	}
}
