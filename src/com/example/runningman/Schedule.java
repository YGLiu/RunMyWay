package com.example.runningman;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
	private HistoryPattern pattern;
	private double num_days;
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
		Button rebuild = (Button) findViewById(R.id.reSchedule);
		rebuild.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				RecomputeSchedule();
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
		.setMessage("Currently you do not have any schedule.\nWould you like to build one now?")
		.setCancelable(false)
		.setPositiveButton("Build",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				ComputeNewSchedule();
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
			if(gotConflictEvents() || gotMissedEvents())
			{	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
				// set title
				alertDialogBuilder.setTitle("Warning");
				// set dialog message and button events
				alertDialogBuilder
				.setMessage("You have conflicted sessions or missed sessions in your schedule.\nWould you like to re-build?")
				.setCancelable(false)
				.setPositiveButton("Re-build",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						RecomputeSchedule();
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
	
	public ArrayList<slot> getEmptySlot()
	{	ArrayList<slot> emptyslots = new ArrayList<slot>();
		ArrayList<slot> occupiedslot = new ArrayList<slot>();
		Date cur = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(cur);
		cal.add(Calendar.DATE, 7);
		Date oneWeek = cal.getTime();
		
		boolean hasHistoryRecord = false;
		boolean sameDay;
		Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableHistory);
		cursor.moveToFirst();
		if(cursor.getInt(0) > 30)
			hasHistoryRecord = true;
		cursor.close();
		
		cursor = DBI.select("SELECT * FROM " + DBI.tableCalendar);
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{	try
			{	Date eventStart = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(0) + cursor.getString(1));
				Date eventEnd = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(0) + cursor.getString(2));
				if(eventEnd.after(cur) && eventStart.before(oneWeek))
					occupiedslot.add(new slot(eventStart,eventEnd));
				cursor.moveToNext();
			}
			catch(Exception e)
			{	e.printStackTrace();}
		}
		cursor.close();
		
		cursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{	try
			{	Date eventStart = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(1) + cursor.getString(2));
				Date eventEnd = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(1) + cursor.getString(3));
				if(eventEnd.after(cur) && eventStart.before(oneWeek))
					occupiedslot.add(new slot(eventStart,eventEnd));
				cursor.moveToNext();
			}
			catch(Exception e)
			{	e.printStackTrace();}
		}
		cursor.close();
		Collections.sort(occupiedslot, new Comparator<slot>() {
			  public int compare(slot o1, slot o2) {
			      return o1.start.compareTo(o2.start);
			  }
		});
		for(int i=0;i<occupiedslot.size();i++)
		{	slot empty;
			if(i == 0)
				empty = new slot(cur,occupiedslot.get(i).start);
			else
			{	if(i == occupiedslot.size()-1)
					empty = new slot(occupiedslot.get(i).end,oneWeek);
				else
					empty = new slot(occupiedslot.get(i).end,occupiedslot.get(i+1).start);
			}
			Calendar calStart = Calendar.getInstance();
			Calendar calEnd = Calendar.getInstance();
			calStart.setTime(empty.start);
			calEnd.setTime(empty.end);
			sameDay = calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
					  calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR);
			if(!sameDay)
			{	calStart.set(Calendar.HOUR_OF_DAY, 23);
				calStart.set(Calendar.MINUTE, 59);
				calStart.set(Calendar.SECOND, 59);
				empty.end = calStart.getTime();
			}
			if(hasHistoryRecord)
			{	if(empty.getDiff() >= AveDuration + 30 && !isBadWeather(empty.date))
					emptyslots.add(empty);
			}
			else
			{	if(empty.getDiff() >= 90 && !isBadWeather(empty.date))
					emptyslots.add(empty);
			}
		}
		return emptyslots;
	}
	public boolean isBadWeather(String date)
	{	
		Cursor cursor = DBI.select("SELECT Weather FROM " + DBI.tableWeather + " WHERE Date = '" + date + "'");
		if(cursor.moveToFirst())
		{	String wea = cursor.getString(0);
			return weather.isWeatherPoorCondition(wea);
		}
		return false;
	}
	public void Compute(int duration)
	{	boolean hasHistoryRecord = false;
		int num;
		Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableHistory);
		cursor.moveToFirst();
		if(cursor.getInt(0) > 30)
			hasHistoryRecord = true;
		cursor.close();
		ArrayList<slot> emptyslots = getEmptySlot();
		for(int i=0;i<emptyslots.size();i++)
			System.out.println(emptyslots.get(i).start + " " + emptyslots.get(i).end);
		System.out.println(AveDuration + " " + num_days);
		if(hasHistoryRecord)
		{	//rank emptyslots based on historypattern
			ArrayList<slot> sortedEmptyslots = new ArrayList<slot>();
			for(int i=0;i<pattern.historyPattern.size();i++)
			{	for(int j=0;j<emptyslots.size();j++)
				{	if(emptyslots.get(j).day.equals(pattern.historyPattern.get(i).day_of_week) && 
						emptyslots.get(j).timeofday.equals(pattern.historyPattern.get(i).time_of_day))
						sortedEmptyslots.add(emptyslots.get(j));
				}
			}
			num = Math.min(sortedEmptyslots.size(), (int) Math.ceil(duration/AveDuration));
			PlanSchedule(sortedEmptyslots,num,AveDuration);
		}
		else
		{	
			for(int i=0;i<emptyslots.size();i++)
			{	if(!emptyslots.get(i).timeofday.equals("evening"))
				{	emptyslots.remove(i);
					i--;
				}
			}
			num = Math.min(emptyslots.size(), (int) Math.ceil(duration/60));
			System.out.println("No history record" + num );
			PlanSchedule(emptyslots,num,60);
		}
	}
	public void PlanSchedule(ArrayList<slot> emptyslots,int num,double duration)
	{	for(int i=0;i<num && i<emptyslots.size();i++)
		{	Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableSchedule + " WHERE Date = '" + emptyslots.get(i).date + "'");
			cursor.moveToFirst();
			if(cursor.getInt(0) > 0)
			{	emptyslots.remove(i);
				i--;
				continue;
			}
			String start = new SimpleDateFormat("HH:mm:ss",Locale.US).format(new Date(emptyslots.get(i).start.getTime() + 15*60*1000));
			String end = new SimpleDateFormat("HH:mm:ss",Locale.US).format(new Date(emptyslots.get(i).start.getTime() + 15*60*1000 + (int)duration*60*1000));
			String date = emptyslots.get(i).date;
			ContentValues CV = new ContentValues();
			CV.put("Date", date);
			CV.put("Start", start);
			CV.put("End", end);
			CV.put("Status", "UPCOMING");
			DBI.insert(DBI.tableSchedule, CV);
			try
			{	setAlarm(new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(date+start).getTime());
			}
			catch(Exception e)
			{	e.printStackTrace();}
		}
	}
	public int removeConflictedEvents()
	{	Cursor cursor = DBI.select("SELECT Id,Start,End FROM " + DBI.tableSchedule + " WHERE Status = 'CONFLICT' OR Status = 'MISSED'");
		cursor.moveToFirst();
		double total = 0;
		while(!cursor.isAfterLast())
		{	try
			{	long diff = (long) new SimpleDateFormat("HH:mm:ss",Locale.US).parse(cursor.getString(2)).getTime() - (long) new SimpleDateFormat("HH:mm:ss",Locale.US).parse(cursor.getString(1)).getTime();
				total += (double) (diff/1000/60);
				DBI.delete(DBI.tableSchedule, "Id = '" + cursor.getString(0) + "'");
				cursor.moveToNext();
			}
			catch(Exception e)
			{	e.printStackTrace();}
		}
		System.out.println("remove conflicted events" + total);
		return (int) Math.ceil(total);
	}
	public void ComputeNewSchedule()
	{	boolean hasHistoryRecord = false;
		Cursor cursor = DBI.select("SELECT COUNT(*) FROM " + DBI.tableHistory);
		cursor.moveToFirst();
		if(cursor.getInt(0) > 30)
			hasHistoryRecord = true;
		cursor.close();
		if(hasHistoryRecord)
			Compute((int)(AveDuration*7/num_days));
		else
			Compute(180);
		displaySchedule();
	}
	public void RecomputeSchedule()
	{	int duration = removeConflictedEvents();
		Compute(duration);
		displaySchedule();
	}
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
			return true;
		else
			return false;
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
	
	private void displaySchedule() 
	{
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
		{	ArrayList<HistoryData> history = new ArrayList<HistoryData>();
			pattern = new HistoryPattern();
			Cursor cursor = DBI.select("SELECT Date,Duration,Start FROM " + DBI.tableHistory);
			cursor.moveToFirst();
			AveDuration = 0;
			double sum = 0;
			num_days = 0;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -180);
			Date a_month_ago = cal.getTime();
			while(!cursor.isAfterLast())
			{	if(new SimpleDateFormat("yyyy-MM-ddHH:mm:ss",Locale.US).parse(cursor.getString(0)+cursor.getString(2)).after(a_month_ago))
				{	HistoryData historyObj = new HistoryData(cursor.getString(0),cursor.getString(2),"",cursor.getDouble(1),0,0); 
					history.add(historyObj);
				}
				cursor.moveToNext();
			}
			cursor.close();
			for(HistoryData data : history)
			{	sum += data.duration;
				pattern.countIncrement(data.date, data.start);
			}
			if(history.size() != 0)
			{	AveDuration = sum / history.size();
				num_days = (double)180/(double)history.size();
			}
			pattern.sort();
			for(int i=0;i<pattern.historyPattern.size();i++)
				System.out.println(pattern.historyPattern.get(i).day_of_week + pattern.historyPattern.get(i).time_of_day + pattern.historyPattern.get(i).frequency);
		}
		catch(Exception e)
		{	e.printStackTrace(); }
	}
	public void setAlarm(long time)
	{	Intent myIntent = new Intent(getApplicationContext(), NotifyService.class);
	    myIntent.putExtra("Message", "You have a running session in 15 minutes");
	    myIntent.setAction("com.example.runningman.NotifyAction");
	    AlarmManager alarms ;
	    PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
	    alarms.set(AlarmManager.RTC, time-15*60*1000, alarmIntent);
	}
}
