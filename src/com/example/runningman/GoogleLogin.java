/*
 * Author:			Simon Wan Wenli
 * Module code:		CS4274
 * Last updated:	26 Oct 2013
 * API:				Android 4.3
 */

package com.example.runningman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

// Google API libraries
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;


public class GoogleLogin extends Activity {
	
	private DBInterface DBI;
	private CalendarList calendarList;
	private Calendar calendarService;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_login);
		// Show the Up button in the action bar.
		setupActionBar();
		// initialization of database interface
		DBI = new DBInterface(this);
		
		// disable Strict Mode and allow networking tasks on main thread
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		// try google calendar API login
		try {
			connectGoogleCalendar();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.google_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// sync with a google account
	// return a list of calendars of the user
	public void connectGoogleCalendar() throws IOException {
		final HttpTransport httpTransport = new NetHttpTransport();
		final JacksonFactory jsonFactory = new JacksonFactory();
		
		// The clientId and clientSecret can be found in Google Cloud Console
		// TODO store the parameters in database instead of hard code
	    final String clientId = "557655694825-7v50n300c4r1grmh2jgpv5iook0n00ki.apps.googleusercontent.com";
	    final String clientSecret = "_VsFRNBgKsOLevgPZSM1RsU2";

	    // your redirect URL for web based applications.
	    final String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
	    
	    // Authorize
	    final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	    		httpTransport, jsonFactory, clientId, clientSecret, Arrays.asList(CalendarScopes.CALENDAR)).setAccessType("online")
	    		.setApprovalPrompt("auto").build();
	    String url = flow.newAuthorizationUrl().setRedirectUri(redirectUrl).build();
	    
	    // open the URL in browser
		Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browser);
		
	    // click on the button
		// exchange authentication code for token
		Button connectGoogleButton = (Button) findViewById(R.id.buttonConnectGoogle);
	    final EditText authCodeEditText = (EditText) findViewById(R.id.editTextAuthCode);

	    connectGoogleButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                String code = authCodeEditText.getText().toString();
                Log.d("Auth code", code);
                
        		try {
        			// get response token
        			GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();       			
        			// Credential Builder
        			GoogleCredential credential = new GoogleCredential.Builder()
        		    .setTransport(new NetHttpTransport())
        		    .setJsonFactory(new JacksonFactory())
        		    .setClientSecrets(clientId, clientSecret)
        		    .addRefreshListener(new CredentialRefreshListener() {
        		    	@Override
        		    	public void onTokenResponse(Credential credential, TokenResponse tokenResponse) {
					        // Handle success.
					        //Log.d("Success:","Credential was refreshed successfully.");
        		    	}
        		    	@Override
        		    	public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
					        // Handle error.
					        //Log.d("Error:", "Credential was not refreshed successfully. "
					           // + "Redirect to error page or login screen.");
        		    	}
				    })
        		    .build();
        			credential.setFromTokenResponse(response);
        			
        			// set up calendar instance
        			// applicationName is compulsory
        			Calendar service = new Calendar.Builder(httpTransport, jsonFactory, credential).
        					setApplicationName("com.example.runningman").build();
        			CalendarList feed = service.calendarList().list().execute();
        			
        			// set values to class variables
        			calendarService = service;
        			calendarList = feed;
        			storeCalendarDB();
        			displayCalendarDB();
        		} catch (IOException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
					calendarService = null;
					calendarList = null;
				}
            }
	        });
	}
	
	private void storeCalendarDB() throws IOException{
		// clear the entire calendar table
		DBI.delete(DBI.tableCalendar, null);
		// traverse every event in a calendar
		for (CalendarListEntry entry : calendarList.getItems()) {
			//Log.d("Calendar ID", entry.getSummary());			
			// set the current time date
			DateTime currDateTime = new DateTime(new Date());			
			// retrieve events only from today onwards
			Events events = calendarService.events().list(entry.getId()).setTimeMin(currDateTime).execute();
			
			// for debugging purpose
			// traverse through all events in a calendar
			for (Event event : events.getItems()) {
				ContentValues cv = null;
				EventDateTime start,end;
				start = event.getStart();
				end = event.getEnd();
				// to avoid nullPointerException
				if (start != null) {
					DateTime startDateTime = start.getDateTime();
					// non-full-day event
					if (startDateTime != null) {
						String startDateTimeString = startDateTime.toString();
						//Log.d("Start Time",startDateTimeString);
						// construct content value
						cv = new ContentValues();
						cv.put("Date", startDateTimeString.substring(0, 10).toString());
						cv.put("Start", startDateTimeString.substring(11, 19));
					}
				}
				if (end != null) {
					DateTime endDateTime = end.getDateTime();
					// non-full-day event
					if (endDateTime != null){
						String endDateTimeString = endDateTime.toString();
						//Log.d("Start Time", endDateTimeString);
						cv.put("End", endDateTimeString.substring(11, 19));
					}
				}				
				// content value might be a null value
				if (cv != null){
					DBI.insert(DBI.tableCalendar, cv);
				}
			}
		}
	}
	
	private void displayCalendarDB(){
		List<String> listValues = new ArrayList<String>();
		String query = "SELECT * FROM " + DBI.tableCalendar + " ORDER BY Date ASC";
		Cursor cursor = DBI.select(query);
		cursor.moveToFirst();		
		while(!cursor.isAfterLast())
		{    
			String date = cursor.getString(0);
			String startTime = cursor.getString(1);
			String endTime = cursor.getString(2);
			String listEntry = date + " " + startTime + " " + endTime;
			listValues.add(listEntry);
			
			cursor.moveToNext();
		}
		// display on the listView
		ListView listview = (ListView) findViewById(R.id.listViewCalendar);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listValues);
		listview.setAdapter(adapter);
	}
}
