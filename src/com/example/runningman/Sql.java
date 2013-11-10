package com.example.runningman;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Sql extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "RunMyWay.db";
	private static final int DATABASE_VERSION = 1;
	// Database creation SQL statement
	private static final String CreateTableUser = "create table User(UID text, Gender text, Height real, Weight real, Birthday text, Target text, Longtitude real DEFAULT 0, Latitude real DEFAULT 0);";
	private static final String CreateTableHistory = "create table History(Date text, Start text, End text, Duration real, Distance real, AveSpeed real);";
	private static final String CreateTableSession = "create table Session(SeqNum int, Lat real, Lng real);";
	private static final String CreateTableCalendar = "create table Calendar(Date text, Start text, End text);";
	private static final String CreateTableSchedule = "create table Schedule(Id int PRIMAY KEY AUTO_INCREMENT, Date text, Start text, End text);";
	private static final String CreateTableWeather = "create table Weather(Date text, Weather text, Temperature real);";
  
  
  public Sql(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(CreateTableUser);
    database.execSQL(CreateTableHistory);
    database.execSQL(CreateTableSession);
    database.execSQL(CreateTableCalendar);
    database.execSQL(CreateTableSchedule);
    database.execSQL(CreateTableWeather);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
  {
	  
  }

} 