package com.example.runningman;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		DBI = new DBInterface(this);
		
		try {
			displaySchedule();
			isRecomputeNeeded();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	// return # collisions if they collide
	public int isScheduleCollidesCalendar() {		
		int conflictCount = 0;
		Cursor scheduleCursor = DBI.select("SELECT * FROM " + DBI.tableSchedule);
		
		// if exist at least one event
		if (scheduleCursor.moveToFirst()) {		
			while(!scheduleCursor.isAfterLast()) {
				String scheduleDate = scheduleCursor.getString(0);
				String query = "SELECT * FROM " + DBI.tableCalendar + "WHERE Date=" + scheduleDate;
				Cursor calendarCursor = DBI.select(query);
				
				// if exist at least one conflicts
				if (calendarCursor.moveToFirst()) {
					while(!scheduleCursor.isAfterLast()) {
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
							if (calStartDate.before(schEndDate) || calEndDate.after(schStartDate)) {
								// increment count by 1
								conflictCount ++;
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return conflictCount;
	}
	
	public int isScheduleCollidesWeather() {
		/*
		 * TODO use Yahoo weather API
		 */
		
		return 0;
	}
	
	public double isProgressBehindSchedule() {
		return 0;
	}
	
	public boolean isRecomputeNeeded() {
		boolean result = false;
		String stats = "";
		int calendarConflictsCount = isScheduleCollidesCalendar();
		int weatherConflictsCount = isScheduleCollidesWeather();
		double behindHrsCount = isProgressBehindSchedule();
		
		if(calendarConflictsCount > 0) {
			result = true;
			stats += Integer.toString(calendarConflictsCount) + " conflicts with caledar. ";
		}
		if(weatherConflictsCount > 0) {
			result = true;
			stats += Integer.toString(weatherConflictsCount) + " conflicts with weather. ";
		}
		if(behindHrsCount > 0) {
			result = true;
			stats += Double.toString(behindHrsCount) + " hours behind the schedule. ";
		}
		if(result) {
			stats += "We suggest you re-schedule your exercise plan.";
		}
		
		// generate dialog to ask user if re-computation is needed
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);		 
		// set title
		alertDialogBuilder.setTitle("Warning");
		// set dialog message and button events
		alertDialogBuilder
		.setMessage(stats)
		.setCancelable(false)
		.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
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
		
		return result;
	}
	
	public void computeSchedule() {
		/*
		* TODO
		* Insert algorithm here
		*/
		
		// this should be the last step of this method
		// display the new schedule
		try {
			displaySchedule();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void displaySchedule() throws Exception {
		List<String> listValues = new ArrayList<String>();
		String query = "SELECT * FROM " + DBI.tableSchedule + " ORDER BY Date ASC";
		Cursor cursor = DBI.select(query);
		Date currentDate = new Date();
		
		cursor.moveToFirst();	
		while(!cursor.isAfterLast())
		{    
			String date = cursor.getString(0);
			String start = cursor.getString(1);
			String end = cursor.getString(2);
			cursor.moveToNext();
			
			Date entryDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
			
			String status;
			if(currentDate.after(entryDate)) {
				status = "PASSED |";
			}
			else {
				status = "TO DO  |";
			}
			String listEntry = status + " " + date + " " + start + " " + end;
			listValues.add(listEntry);
		}
		// display on the listView
		ListView listview = (ListView) findViewById(R.id.listViewSchedule);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listValues);
		listview.setAdapter(adapter);
	}
}
