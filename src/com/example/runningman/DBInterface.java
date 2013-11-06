package com.example.runningman;
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
  
  public void verboseCalendarTable() {
	  String query = "SELECT * FROM " + this.tableCalendar;
	  Cursor csr = this.select(query);
	  if (csr.moveToFirst()) {
		  while(!csr.isAfterLast()) {
			  String date = csr.getString(0);
			  String start = csr.getString(1);
			  String end = csr.getString(2);
			  String line = date + " " + start + " " + end;
			  Log.v(Integer.toString(csr.getPosition()), line);
			  csr.moveToNext();
		  }
	  }
	  else {
		  Log.v("[empty table]", "Empty Calendar Table.");
	  }
  }
} 