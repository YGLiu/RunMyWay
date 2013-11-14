package com.example.runningman;

public class HistoryPatternObj {
	public String day_of_week;
	public int frequency;
	public String time_of_day;
	public HistoryPatternObj(String day_of_week, String time_of_day)
	{
		this.day_of_week = day_of_week;
		this.time_of_day = time_of_day;
		frequency = 0;
	}
	public void incFrequency()
	{
		this.frequency++;
	}
}
