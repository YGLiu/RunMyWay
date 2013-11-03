package com.example.runningman;

public class HistoryData {
	public String date;
	public String start;
	public String end;
	public double duration;
	public double distance;
	public double aveSpeed;
	
	public HistoryData(String date,String start,String end,double duration,double distance,double aveSpeed)
	{	this.date = date;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.distance = distance;
		this.aveSpeed = aveSpeed;
	} 
	
}
