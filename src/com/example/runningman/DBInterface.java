package com.example.runningman;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBInterface{

  private SQLiteDatabase database;
  private Sql dbHelper;
  public String tableUser = "User";
  public String tableHistory = "History";
  public String tableSession = "Session";
  public String tableCalendar = "Calendar";
  public String tableSchedule = "Schedule";
  public String tableWeather = "Weather";

  public DBInterface(Context context) 
  {
	  dbHelper = new Sql(context);
	  database = dbHelper.getWritableDatabase();
  }
  public long insert(String table, ContentValues CV)
  {		
	  	return database.insert(table, null, CV);
  }
  public int delete(String table,String whereClause)
  {
  		return database.delete(table, whereClause, null);
  }
  public Cursor select(String query)
  {		Cursor cursor = null;
  		cursor = database.rawQuery(query,null);
  		return cursor;
  }
  public int update(String table,ContentValues CV, String whereClause)
  {	
  		return database.update(table,CV,whereClause,null);
  }
  
  // for debugging purpose, verbose a table
  public void verboseTable(String tableName) {
	  if (tableName.equals(this.tableCalendar)) {
		  String query = "SELECT * FROM " + this.tableCalendar;
		  Cursor csr = this.select(query);
		  if (csr.moveToFirst()) {
			  while(!csr.isAfterLast()) {
				  String date = csr.getString(0);
				  String start = csr.getString(1);
				  String end = csr.getString(2);
				  String line = date + " " + start + " " + end;
				  Log.v(this.tableCalendar + "[" + Integer.toString(csr.getPosition()) + "]", line);
				  csr.moveToNext();
			  }
		  }
		  else {
			  Log.v("[empty table]", "Empty " + this.tableCalendar + " Table.");
		  }
	  }
	  else if (tableName.equals(this.tableSchedule)) {
		  String query = "SELECT * FROM " + this.tableSchedule;
		  Cursor csr = this.select(query);
		  if (csr.moveToFirst()) {
			  while(!csr.isAfterLast()) {
				  int id = csr.getInt(0);
				  String date = csr.getString(1);
				  String start = csr.getString(2);
				  String end = csr.getString(3);
				  String line = Integer.toString(id) + " " + date + " " + start + " " + end;
				  Log.v(this.tableSchedule + "[" + Integer.toString(csr.getPosition()) + "]", line);
				  csr.moveToNext();
			  }
		  }
		  else {
			  Log.v("[empty table]", "Empty " + this.tableSchedule + " Table.");
		  }
	  }
	 else if (tableName.equals(this.tableHistory)) {
		  String query = "SELECT * FROM " + this.tableHistory;
		  Cursor csr = this.select(query);
		  if (csr.moveToFirst()) {
			  while(!csr.isAfterLast()) {
				  String date = csr.getString(0);
				  String start = csr.getString(1);
				  String end = csr.getString(2);
				  String duration = Double.toString(csr.getDouble(3));
				  String dist = Double.toString(csr.getDouble(4));
				  String aveSpd = Double.toString(csr.getDouble(5));
				  String line = date + " " + start + " " + end + " " + duration + " " + dist + 
						  " " + aveSpd;
				  Log.v(this.tableHistory + "[" + Integer.toString(csr.getPosition()) + "]", line);
				  csr.moveToNext();
			  }
		  }
		  else {
			  Log.v("[empty table]", "Empty " + this.tableHistory + " Table.");
		  }
	  }
  }
  
  public void dummyHistory(boolean clearFlag) {
	  if (clearFlag) {
		  this.delete(this.tableHistory, null);
	  }
	  
	  int evenDaysCount = 0;
	  int oddDaysCount = 0;
	  Date date = new Date();
	  Calendar cal = Calendar.getInstance();
	  cal.setTime(date);
	  Stack<Date> dateStk = new Stack<Date>();
	  
	  while(evenDaysCount < 60) {
		  SimpleDateFormat dayParser = new SimpleDateFormat("EEE", Locale.US);		  
		  String day = dayParser.format(date);
		  		  
		  if(day.equalsIgnoreCase("tue") || day.equalsIgnoreCase("thu") || day.equalsIgnoreCase("sat")) {
			  evenDaysCount++;
			  dateStk.push(date);
		  }
		  else if (oddDaysCount < 40) {
			  oddDaysCount++;
			  dateStk.push(date);
		  }
		  cal.add(Calendar.DATE, -1);
		  date = cal.getTime();
	  }
	  
	  // use stack to reverse the date order
	  while(!dateStk.empty()) {
		  String startTime = "20:00:00";
		  String endTime = "22:00:00";
		  double duration = 120;
		  final double minDist = 2 * 7000;
		  final double maxDist = 2 * 11000;
		  double dist = minDist + (Math.random() * (maxDist - minDist));
		  double aveSpeed = dist / 2000;		  
		  SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);		  	  
		  ContentValues cv = new ContentValues();
		  String dateString = dateParser.format(dateStk.pop());
		  cv.put("Date", dateString);
		  Log.d("date", dateString);
		  cv.put("Start", startTime);
		  cv.put("End", endTime);
		  cv.put("Duration", duration);
		  cv.put("Distance", dist);
		  cv.put("AveSpeed", aveSpeed);
		  this.insert(tableHistory, cv);
	  }
	  this.verboseTable(tableHistory);
  }
} 