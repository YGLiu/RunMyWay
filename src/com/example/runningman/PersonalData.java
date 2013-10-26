package com.example.runningman;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

public class PersonalData extends Activity {

	private DBInterface DBI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_data);
		DBI = new DBInterface(this);
		loadPersonalDataFromDB();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_data, menu);
		return true;
	}
	
	public void loadPersonalDataFromDB() {
		String query = "SELECT * FROM " + DBI.tableUser;
		Cursor cursor = DBI.select(query);
		
		EditText nameEditText = (EditText) findViewById(R.id.editTextUserName);
		RadioGroup genderRadioGrp = (RadioGroup) findViewById(R.id.radioGroupGender);
		EditText heightEditText = (EditText) findViewById(R.id.editTextHeight);
		EditText weightEditText = (EditText) findViewById(R.id.editTextWeight);
		EditText bdayEditText = (EditText) findViewById(R.id.editTextBirthday);
		RadioGroup targetRadioGrp = (RadioGroup) findViewById(R.id.radioGroupPurpose);
		
		// if a user exist, load from database
		if (cursor.getCount() != 0){
			// get user info from database
			cursor.moveToFirst();
			String uid = cursor.getString(0);
			String gender = cursor.getString(1);
			double height = cursor.getDouble(2);
			double weight = cursor.getDouble(3);
			String bday = cursor.getString(4);
			String target = cursor.getString(5);
			
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
			
		}
		// if not, do not touch the GUI.
	}
	
	public void savePersonalDataInDB(View view) {
		EditText nameEditText = (EditText) findViewById(R.id.editTextUserName);
		RadioGroup genderRadioGrp = (RadioGroup) findViewById(R.id.radioGroupGender);
		EditText heightEditText = (EditText) findViewById(R.id.editTextHeight);
		EditText weightEditText = (EditText) findViewById(R.id.editTextWeight);
		EditText bdayEditText = (EditText) findViewById(R.id.editTextBirthday);
		RadioGroup targetRadioGrp = (RadioGroup) findViewById(R.id.radioGroupPurpose);
		
		String uid = null;
		try {
			uid = nameEditText.getText().toString();
		} catch(Exception e) {
			showAlert("Error", "Name is mandatory");
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
			showAlert("Error", "Height is mandatory");
		}
		
		double weight = -1;
		try {
			weight = Double.parseDouble(weightEditText.getText().toString());
		} catch (Exception e) {
			showAlert("Error", "Weight is mandatory");
		}
		
		String bday = null;
		try {
			bday = bdayEditText.getText().toString();
		} catch (Exception e) {
			showAlert("Error", "Birthday is mandatory");
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
		
		if (uid != null && height > 0 && weight > 0 && bday != null) {
			ContentValues cv = new ContentValues();
			cv.put("UID", uid);
			cv.put("Gender", gender);
			cv.put("Height", height);
			cv.put("Weight", weight);
			cv.put("Birthday", bday);
			cv.put("Target", target);
			showAlert("Notification", "Updates saved.");
			// clear the database
			DBI.delete(DBI.tableUser, null);
			DBI.insert(DBI.tableUser, cv);
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
	
	public void GoogleLogin(View view) {
    	Intent intent = new Intent(this, GoogleLogin.class);
    	startActivity(intent);
    }

}
