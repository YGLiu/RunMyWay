package com.example.runningman;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

public class PersonalData extends Activity implements LocationListener{

	private DBInterface DBI;
	private double Lat;
	private double Lng;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_data);
		DBI = new DBInterface(this);
		getCurrentLocation();
		loadPersonalDataFromDB();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_data, menu);
		return true;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		// close database connection
		if (this.DBI != null) {
			DBI.close();
		}
	}
	
	public void loadPersonalDataFromDB() {
		String query = "SELECT * FROM " + DBI.tableUser;
		Cursor cursor = DBI.select(query);
		
		// debugging purpose
		// Log.d("# rows", Integer.toString(cursor.getCount()));
		
		EditText nameEditText = (EditText) findViewById(R.id.editTextUserName);
		RadioGroup genderRadioGrp = (RadioGroup) findViewById(R.id.radioGroupGender);
		EditText heightEditText = (EditText) findViewById(R.id.editTextHeight);
		EditText weightEditText = (EditText) findViewById(R.id.editTextWeight);
		EditText bdayEditText = (EditText) findViewById(R.id.editTextBirthday);
		RadioGroup targetRadioGrp = (RadioGroup) findViewById(R.id.radioGroupPurpose);
		
		// if a user exist, load from database
		if (cursor.moveToFirst()){
			// get user info from database
			String uid = cursor.getString(0);
			String gender = cursor.getString(1);
			double height = cursor.getDouble(2);
			double weight = cursor.getDouble(3);
			String bday = cursor.getString(4);
			String target = cursor.getString(5);
			double longtitude = cursor.getDouble(6);
			double latitude = cursor.getDouble(7);
			
			// set values in the views
			nameEditText.setText(uid);
			
			if (gender.equals("Male")){
				genderRadioGrp.check(R.id.radioMale);
			}
			else{
				genderRadioGrp.check(R.id.radioFemale);
			}
			heightEditText.setText(String.valueOf(height));
			weightEditText.setText(String.valueOf(weight));
			bdayEditText.setText(bday);
			
			if (target.equals("Exercise")){
				targetRadioGrp.check(R.id.radioExercise);
			}
			else if (target.equals("Lose")){
				targetRadioGrp.check(R.id.radioLoseWeight);
			}
			else {
				targetRadioGrp.check(R.id.radioForFun);
			}
			
			// Log.d("longtitude", Double.toString(longtitude));
			// Log.d("latitude", Double.toString(latitude));
			
			// check if the home address has been set or not
			if (longtitude == 0 && latitude == 0) {
				String msg = "You have not set your home address. " +
						"Would you like to set the current address as your home?";
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);				 
				// set title
				alertDialogBuilder.setTitle("Warning");
				// set dialog message
				alertDialogBuilder
					.setMessage(msg)
					.setCancelable(false)
					.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// save home location
							writeLocationDB();
						}
					})
					.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							dialog.cancel();
						}
				});
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create(); 
				// show it
				alertDialog.show();
			}			
		}
		cursor.close();
		// if not, do not touch the GUI.
	}
	
	public void clickSaveButton(View view) {
		this.savePersonalDataInDB();	
	}
	
	public void clickUpdateHomeLocButton(View view) {
		this.writeLocationDB();
	}
	
	public void savePersonalDataInDB() {
		boolean isInputValid = true;
		String alertMsg = "";
		SimpleDateFormat dateParser = new SimpleDateFormat("ddMMyyyy", Locale.US);
		EditText nameEditText = (EditText) findViewById(R.id.editTextUserName);
		RadioGroup genderRadioGrp = (RadioGroup) findViewById(R.id.radioGroupGender);
		EditText heightEditText = (EditText) findViewById(R.id.editTextHeight);
		EditText weightEditText = (EditText) findViewById(R.id.editTextWeight);
		EditText bdayEditText = (EditText) findViewById(R.id.editTextBirthday);
		RadioGroup targetRadioGrp = (RadioGroup) findViewById(R.id.radioGroupPurpose);		
		
		String uid = nameEditText.getText().toString();
		if (uid.equals("")) {
			isInputValid = false;
			alertMsg += "\nName is mandatory.";
		}
		
		String gender = null;
		int genderRadioBtnId = genderRadioGrp.getCheckedRadioButtonId();
		if (genderRadioBtnId == R.id.radioMale){
			gender = "Male";
		}
		else{
			gender = "Female";
		}
		
		double height = -1;
		try {
			height = Double.parseDouble(heightEditText.getText().toString());
		} catch (Exception e) {
			isInputValid = false;
			alertMsg += "\nHeight is mandatory.";
		}
		
		double weight = -1;
		try {
			weight = Double.parseDouble(weightEditText.getText().toString());
		} catch (Exception e) {
			isInputValid = false;
			alertMsg += "\nWeight is mandatory.";
		}
		
		String bday = bdayEditText.getText().toString();
		if (bday.equals("")) {
			isInputValid = false;
			alertMsg += "\nBirthday is mandatory";
		}
		try {
			dateParser.parse(bday);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			isInputValid = false;
			alertMsg += "\nInvalid birthday format.";
			e.printStackTrace();
		}
		
		String target;
		int targetRadioBtnId = targetRadioGrp.getCheckedRadioButtonId();
		if (targetRadioBtnId == R.id.radioExercise){
			target = "Exercise";
		}
		else if (targetRadioBtnId == R.id.radioLoseWeight){
			target = "Lose";
		}
		else {
			target = "Fun";
		}
		
		if (isInputValid) {
			ContentValues cv = new ContentValues();
			cv.put("UID", uid);
			cv.put("Gender", gender);
			cv.put("Height", height);
			cv.put("Weight", weight);
			cv.put("Birthday", bday);
			cv.put("Target", target);
			
			String query = "SELECT * FROM " + DBI.tableUser;
			Cursor cursor = DBI.select(query);
			// if table is not empty, update it
			if (cursor.getCount() != 0) {
				DBI.update(DBI.tableUser, cv, null);
			}
			// if table is empty, insert personal data
			else {
				DBI.insert(DBI.tableUser, cv);
			}
			cursor.close();
			showAlert("Notification", "Personal data updated.");
			// load the database again and check availability of home address
			this.loadPersonalDataFromDB();
		}
		else {
			showAlert("Error", alertMsg);
		}
	}
	
	private void showAlert(String title, String message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set title
		alertDialogBuilder.setTitle(title);
		// set dialog message
		alertDialogBuilder
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					dialog.cancel();
				}
			})
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					dialog.cancel();
				}
		});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create(); 
		// show it
		alertDialog.show();
	}
	
	private void getCurrentLocation() {	
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		if(location!=null)
		    onLocationChanged(location);
		locationManager.requestLocationUpdates(provider, 1000, 0, this);
	}
	
	// write Lng and Lat into DB
	private void writeLocationDB() {
		ContentValues cv = new ContentValues();
		cv.put("Longtitude", this.Lng);
		cv.put("Latitude", this.Lat);
		
		String query = "SELECT * FROM " + DBI.tableUser;
		Cursor cursor = DBI.select(query);
		// if table is not empty, update it
		if (cursor.getCount() != 0) {
			DBI.update(DBI.tableUser, cv, null);
			showAlert("Notification", "Home location updated.");
		}
		// if table is empty, insert home location
		else {
			// DBI.insert(DBI.tableUser, cv);
			showAlert("Alert", "Personal Data cannot be left empty.");
		}
		cursor.close();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Lat = location.getLatitude();
		Lng = location.getLongitude();
	}
	
	// three functions below need to be present
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
