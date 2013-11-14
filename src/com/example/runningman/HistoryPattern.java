package com.example.runningman;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryPattern {
	public ArrayList<HistoryPatternObj> historyPattern;
	public HistoryPattern()
	{	historyPattern = new ArrayList<HistoryPatternObj>();
		historyPattern.add(new HistoryPatternObj("Mon","morning"));
		historyPattern.add(new HistoryPatternObj("Mon","afternoon"));
		historyPattern.add(new HistoryPatternObj("Mon","evening"));
		historyPattern.add(new HistoryPatternObj("Mon","midnight"));
		historyPattern.add(new HistoryPatternObj("Tue","morning"));
		historyPattern.add(new HistoryPatternObj("Tue","afternoon"));
		historyPattern.add(new HistoryPatternObj("Tue","evening"));
		historyPattern.add(new HistoryPatternObj("Tue","midnight"));
		historyPattern.add(new HistoryPatternObj("Wed","morning"));
		historyPattern.add(new HistoryPatternObj("Wed","afternoon"));
		historyPattern.add(new HistoryPatternObj("Wed","evening"));
		historyPattern.add(new HistoryPatternObj("Wed","midnight"));
		historyPattern.add(new HistoryPatternObj("Thu","morning"));
		historyPattern.add(new HistoryPatternObj("Thu","afternoon"));
		historyPattern.add(new HistoryPatternObj("Thu","evening"));
		historyPattern.add(new HistoryPatternObj("Thu","midnight"));
		historyPattern.add(new HistoryPatternObj("Fri","morning"));
		historyPattern.add(new HistoryPatternObj("Fri","afternoon"));
		historyPattern.add(new HistoryPatternObj("Fri","evening"));
		historyPattern.add(new HistoryPatternObj("Fri","midnight"));
		historyPattern.add(new HistoryPatternObj("Sat","morning"));
		historyPattern.add(new HistoryPatternObj("Sat","afternoon"));
		historyPattern.add(new HistoryPatternObj("Sat","evening"));
		historyPattern.add(new HistoryPatternObj("Sat","midnight"));
		historyPattern.add(new HistoryPatternObj("Sun","morning"));
		historyPattern.add(new HistoryPatternObj("Sun","afternoon"));
		historyPattern.add(new HistoryPatternObj("Sun","evening"));
		historyPattern.add(new HistoryPatternObj("Sun","midnight"));
	}
	@SuppressWarnings("deprecation")
	public void countIncrement(String date, String start)
	{	try
		{	String day_of_week, time_of_day = null;
			day_of_week = new SimpleDateFormat("EEE",Locale.US).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(date));
			if(0 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() < 6)
				time_of_day = "morning";
			if(6 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() < 12)
				time_of_day = "afternoon";
			if(12 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() < 18)
				time_of_day = "evening";
			if(18 <= new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() && new SimpleDateFormat("HH:mm:ss",Locale.US).parse(start).getHours() <= 23)
				time_of_day = "midnight";
			for(HistoryPatternObj obj : historyPattern)
				if(obj.day_of_week.equals(day_of_week) && obj.time_of_day.equals(time_of_day))
					obj.incFrequency();
		}
		catch(Exception e)
		{	e.printStackTrace();}
	}
}
