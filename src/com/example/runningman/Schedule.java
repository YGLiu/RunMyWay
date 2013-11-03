package com.example.runningman;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
	private int num_days;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		DBI = new DBInterface(this);
		getHistoryPattern();
		displaySchedule();
		isRecomputeNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	// return # collisions if they collide
	@SuppressLint("SimpleDateFormat")
	public int isScheduleCollidesCalendar() {
		int conflictCount = 0;
		Cursor scheduleCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		
		// if exist at least one event
		if (scheduleCursor.moveToFirst()) {	
			while(!scheduleCursor.isAfterLast()) {
				String scheduleDate = scheduleCursor.getString(0);				
				// select events from calendar which have the same date
				String query = "SELECT * FROM " + DBI.tableCalendar + " WHERE Date='" + scheduleDate + "'";			
				Cursor calendarCursor = DBI.select(query);
				
				if (calendarCursor.moveToFirst()) {
					while(!calendarCursor.isAfterLast()) {
						// conversion to Date type
						String calStart = calendarCursor.getString(1);
						String schStart = scheduleCursor.getString(1);
						String calEnd = calendarCursor.getString(2);
						String schEnd = scheduleCursor.getString(2); 
						
						try {
							SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
							Date calStartDate = parser.parse(calStart);
							Date calEndDate = parser.parse(calEnd);
							Date schStartDate = parser.parse(schStart);
							Date schEndDate = parser.parse(schEnd);
							// if event slot has overlapping part
							if ((calStartDate.before(schEndDate) && calStartDate.after(schStartDate)) ||
									calStartDate.equals(schStartDate) || calStartDate.equals(schEndDate) ||
									(calEndDate.before(schEndDate) && calEndDate.after(schStartDate)) ||
									calEndDate.equals(schStartDate) || calEndDate.equals(schEndDate)) {
								// increment conflict count by 1
								conflictCount ++;
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
		return conflictCount;
	}
	
	public int isScheduleCollidesWeather() {
		/*
		 * TODO use Yahoo weather API
		 * Assigned to: Hu Yang
		 */
		return 0;
	}
	
	// return # hours of history behind planned schedule
	@SuppressLint("SimpleDateFormat")
	public double isProgressBehindSchedule() {
		double hisSum = 0, schSum = 0;
		double hrsBehind = 0;
		Cursor schCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		Cursor hisCursor = DBI.select("SELECT * FROM " + DBI.tableHistory);
		Date currDate = new Date();
		SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
		
		// calculate scheduled exercise duration before today
		if (schCursor.moveToFirst()) {
			while(!schCursor.isAfterLast()) {
				try {				
					String schDate = schCursor.getString(0);
					String schStart = schCursor.getString(1);
					String schEnd = schCursor.getString(2);					
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
	
	public boolean isRecomputeNeeded() {
		boolean result = false;
		String stats = "";
		int calendarConflictsCount = isScheduleCollidesCalendar();
		int weatherConflictsCount = isScheduleCollidesWeather();
		double behindHrsCount = isProgressBehindSchedule();
		
		if(calendarConflictsCount > 0) {
			result = true;
			stats += Integer.toString(calendarConflictsCount) + " conflicts with calendar.\n";
		}
		if(weatherConflictsCount > 0) {
			result = true;
			stats += Integer.toString(weatherConflictsCount) + " conflicts with weather.\n";
		}
		if(behindHrsCount > 0) {
			result = true;
			stats += Double.toString(behindHrsCount) + " hours behind the schedule.\n";
		}
		
		if(result) {
			stats += "We suggest you reschedule your exercise plan.";
			
			// generate dialog to ask user if re-computation is needed
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
			// set title
			alertDialogBuilder.setTitle("Warning");
			// set dialog message and button events
			alertDialogBuilder
			.setMessage(stats)
			.setCancelable(false)
			.setPositiveButton("Reschedule",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					computeSchedule();
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
		return result;
	}
	
	public void computeSchedule() {
		/*
		* TODO
		* Insert algorithm here
		* Assigned to: Liu Yaguang
		*/
		
		// this should be the last step of this method
		// display the new schedule
		displaySchedule();
	}
	
	@SuppressLint("SimpleDateFormat")
	private void displaySchedule() {
		// debugging purpose, remove Schedule table
		// DBI.delete(DBI.tableSchedule, null);
		List<String> listValues = new ArrayList<String>();
		String query = "SELECT * FROM " + DBI.tableSchedule + " ORDER BY Date ASC";
		Cursor cursor = DBI.select(query);
		Date currentDate = new Date();
		
		// if Schedule table is not empty
		if (cursor.moveToFirst()) {
			while(!cursor.isAfterLast())
			{	
				String date = cursor.getString(0);
				String start = cursor.getString(1);
				String end = cursor.getString(2);
				cursor.moveToNext();
				try {
					Date entryDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);					
					String status;
					if(currentDate.after(entryDate)) {
						status = "PASSED |";
					}
					else {
						status = "TO DO    |";
					}
					String listEntry = status + " " + date + " " + start + " " + end;
					listValues.add(listEntry);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			// display on the listView
			ListView listview = (ListView) findViewById(R.id.listViewSchedule);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listValues);
			listview.setAdapter(adapter);
		}
		// if Schedule table is empty, prompt user to build one
		else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
			// set title
			alertDialogBuilder.setTitle("Warning");
			// set dialog message and button events
			alertDialogBuilder
			.setMessage("Currently you do not have a schedule.\nWould you like to build one?")
			.setCancelable(false)
			.setPositiveButton("Build",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					computeSchedule();
					// recursively call this again to display Schedule
					displaySchedule();
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
	private void getHistoryPattern()
	{	try
		{	ArrayList<HistoryData> historyData = new ArrayList<HistoryData>();
			Cursor cursor = DBI.select("SELECT Date,Duration FROM " + DBI.tableHistory);
			cursor.moveToFirst();
			AveDuration = 0;
			double sum = 0;
			int count = 0;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -30);
			Date a_month_ago = cal.getTime();
			count_Mon = count_Tue = count_Wed = count_Thu = count_Fri = count_Sat = count_Sun = 0;
			while(!cursor.isAfterLast())
			{	HistoryData history = new HistoryData(cursor.getString(0),"","",cursor.getDouble(1),0,0); 
				historyData.add(history);
				cursor.moveToNext();
			}
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
			}
			AveDuration = sum / historyData.size();
			if(count != 0)
				num_days = 30 / count;
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
}
