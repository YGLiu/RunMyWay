package com.example.runningman;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class slot {
	public Date start;
	public Date end;
	public String day;
	public String timeofday;
	@SuppressWarnings("deprecation")
	public slot(Date start,Date end)
	{	this.start = start;
		this.end = end;
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Mon"))
			this.day = "Mon";
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Tue"))
			this.day = "Tue";
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Wed"))
			this.day = "Wed";
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Thu"))
			this.day = "Thu";
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Fri"))
			this.day = "Fri";
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Sat"))
			this.day = "Sat";
		if(new SimpleDateFormat("EEE",Locale.US).format(start).equals("Sun"))
			this.day = "Sun";
		if(0 <= start.getHours() && start.getHours() < 6)
			this.timeofday = "midnight";
		if(6 <= start.getHours() && start.getHours() < 12)
			this.timeofday = "morning";
		if(12 <= start.getHours() && start.getHours() < 18)
			this.timeofday = "afternoon";
		if(18 <= start.getHours() && start.getHours() <= 23)
			this.timeofday = "evening";
	}
}
