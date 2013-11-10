package com.example.runningman;

import android.content.ContextWrapper;

import com.buzzbox.mob.android.scheduler.NotificationMessage;
import com.buzzbox.mob.android.scheduler.Task;
import com.buzzbox.mob.android.scheduler.TaskResult;

public class MyTask  implements Task{
	
	@Override
	public TaskResult doWork(ContextWrapper ctx) {
	            
	    // write here your code
		
	    
	    //1. create a TaskResult                
	    TaskResult res = new TaskResult();
	    
	    //2. create a Notification
	    NotificationMessage notification = new NotificationMessage(
	    	"New Email", 
	    	"You have a new message from Lucy");
	    
	    //3. set details
	    notification.setNotificationSettings(true, true, true); // sound, vibrate, led
	    notification.setNotificationClickIntentClass(MainActivity.class);
	    notification.setBgColor("#55ff0000");  // <-- set a custom background color
	    notification.setFlagResource(R.drawable.tag_blue); // <-- set a custom flag image

	    //4. add the notification to the result

	    res.addMessage( notification );    
	    return res;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
