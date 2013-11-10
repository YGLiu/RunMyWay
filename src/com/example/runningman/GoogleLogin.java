/*
 * Author:			Simon Wan Wenli
 * Module code:		CS4274
 * Last updated:	26 Oct 2013
 * API:				Android 4.3
 */

package com.example.runningman;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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
	private GoogleAuthorizationCodeFlow flow;
	private CalendarList calendarList;
	private Calendar calendarService;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_login);
		// Show the Up button in the action bar
		setupActionBar();
		// initialization of database interface
		DBI = new DBInterface(this);

		final WebView webview = new WebView(this);
		
		try {
			String url = this.getGoogleAuthRedirUrl();
			webview.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int progress) {
					// Activities and WebViews measure progress with different scales.
					// The progress meter will automatically disappear when we reach 100%
					setProgress(progress * 1000);
				}
			});
			
			webview.getSettings().setJavaScriptEnabled(true);
			webview.addJavascriptInterface(new JsInterface(this), "authCodeGetter");
			webview.loadUrl(url);
			setContentView(webview);
			
			webview.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					CookieSyncManager.getInstance().sync();
					// Get the cookie from cookie jar.
					String title = view.getTitle();
									
					String[] parts = title.split("=");
					if (parts[0].equalsIgnoreCase("Success code")) {
					    String jsCmd = "javascript:window.authCodeGetter.getAuthCodeFromHTML" +
					    		"(document.getElementById('code').value);";
					    Log.d("[command]", jsCmd);
					    webview.loadUrl(jsCmd);
					    GoogleLogin.this.finish();
					}
				}
			});
			// terminate the activity
			// finish();
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
	 
	class JsInterface {
		private Context ctx;
		// constructor
		JsInterface(Context ctx) {
			this.ctx = ctx;
		}
		
		@JavascriptInterface
		public void getAuthCodeFromHTML(String data) {
			String authCode = data;
			Log.d("HTML", authCode);
			getCalendarFeed(authCode);
		    storeCalendarDB();
		    DBI.verboseTable(DBI.tableCalendar);
		}
	}
	
	// sync with a google account
	// return a list of calendars of the user
	public String getGoogleAuthRedirUrl() throws IOException {
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
	    this.flow = flow;
	    return url;
	}
	
	private void getCalendarFeed(String authCode) {
		final HttpTransport httpTransport = new NetHttpTransport();
		final JacksonFactory jsonFactory = new JacksonFactory();
		
		// The clientId and clientSecret can be found in Google Cloud Console
		// TODO store the parameters in database instead of hard code
	    final String clientId = "557655694825-7v50n300c4r1grmh2jgpv5iook0n00ki.apps.googleusercontent.com";
	    final String clientSecret = "_VsFRNBgKsOLevgPZSM1RsU2";

	    // your redirect URL for web based applications.
	    final String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
		
		try {
	    // get response token
		GoogleTokenResponse response = this.flow.newTokenRequest(authCode).setRedirectUri(redirectUrl).execute();       			
		// Credential Builder
		GoogleCredential credential = new GoogleCredential.Builder()
	    .setTransport(new NetHttpTransport())
	    .setJsonFactory(new JacksonFactory())
	    .setClientSecrets(clientId, clientSecret)
	    .addRefreshListener(new CredentialRefreshListener() {
	    	@Override
	    	public void onTokenResponse(Credential credential, TokenResponse tokenResponse) {
		        // Handle success.
		        Log.d("Success:","Credential was refreshed successfully.");
	    	}
	    	@Override
	    	public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
		        // Handle error.
		        Log.d("Error:", "Credential was not refreshed successfully. "
		            + "Redirect to error page or login screen.");
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
		this.calendarService = service;
		this.calendarList = feed;
		} catch (IOException e) {
			e.printStackTrace();
			this.calendarService = null;
			this.calendarList = null;
		}
	}
	
	private void storeCalendarDB() {
		// clear the entire calendar table
		DBI.delete(DBI.tableCalendar, null);
		// traverse every event in a calendar
		for (CalendarListEntry entry : calendarList.getItems()) {
			Log.d("Calendar ID", entry.getSummary());			
			// set the current time date
			DateTime currDateTime = new DateTime(new Date());			
			// retrieve events only from today onwards
			
			try {
				Events events = calendarService.events().list(entry.getId()).setTimeMin(currDateTime).execute();
				// traverse through all events in a calendar
				for (Event event : events.getItems()) {
					ContentValues cv = null;
					EventDateTime start,end;
					start = event.getStart();
					end = event.getEnd();
					// to avoid nullPoinhttp://drive.google.com/terException
					if (start != null) {
						DateTime startDateTime = start.getDateTime();
						// non-full-day event
						if (startDateTime != null) {
							String startDateTimeString = startDateTime.toString();
							Log.d("Start Time",startDateTimeString);
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
							Log.d("End Time", endDateTimeString);
							cv.put("End", endDateTimeString.substring(11, 19));
						}
					}				
					// content value might be a null value
					if (cv != null){
						DBI.insert(DBI.tableCalendar, cv);
					}
				}
			Toast.makeText(getApplicationContext(), "Calendar Updated", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/*
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
	*/
}
