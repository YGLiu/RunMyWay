package com.example.runningman;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.tools.AsciiUtils;
import com.example.weather.tools.ImageUtils;
import com.example.weather.tools.NetworkUtils;
import com.example.weather.utils.YahooWeather4a.WeatherInfo;
import com.example.weather.utils.YahooWeather4a.WeatherInfo.ForecastInfo;
import com.example.weather.utils.YahooWeather4a.YahooWeatherInfoListener;
import com.example.weather.utils.YahooWeather4a.YahooWeatherUtils;

public class Weather extends Activity implements YahooWeatherInfoListener {
	
	private ImageView ivWeather0;
	private ImageView ivWeather1;
	private ImageView ivWeather2;
	private TextView tvWeather0;
	private TextView tvWeather1;
	private TextView tvWeather2;
	private TextView tvErrorMessage;
	private TextView tvTitle;
	//private EditText etAreaOfCity;
	//private Button btSearch;
	private YahooWeatherUtils yahooWeatherUtils = YahooWeatherUtils.getInstance();
    private String location = "Singapore";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		// Show the Up button in the action bar.
		setupActionBar();
		
		if (!NetworkUtils.isConnected(getApplicationContext())) {
        	Toast.makeText(getApplicationContext(), "Network connection is unavailable!!", Toast.LENGTH_SHORT).show();
        	return;
        }        
    	tvTitle = (TextView) findViewById(R.id.textview_title);
		tvWeather0 = (TextView) findViewById(R.id.textview_weather_info_0);
		tvWeather1 = (TextView) findViewById(R.id.textview_weather_info_1);
		tvWeather2 = (TextView) findViewById(R.id.textview_weather_info_2);
		tvErrorMessage = (TextView) findViewById(R.id.textview_error_message);
		ivWeather0 = (ImageView) findViewById(R.id.imageview_weather_info_0);
		ivWeather1 = (ImageView) findViewById(R.id.imageview_weather_info_1);
		ivWeather2 = (ImageView) findViewById(R.id.imageview_weather_info_2);
        
        String convertedlocation = AsciiUtils.convertNonAscii(location);
        yahooWeatherUtils.queryYahooWeather(getApplicationContext(), convertedlocation, this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
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
	@Override
	public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if(weatherInfo != null) {
        	setNormalLayout();
			tvTitle.setText(weatherInfo.getTitle() + "\n"
					+ weatherInfo.getLocationCity() + ", "
					+ weatherInfo.getLocationCountry());
			tvWeather0.setText("====== CURRENT ======" + "\n" +
					           "date: " + weatherInfo.getCurrentConditionDate() + "\n" +
							   "weather: " + weatherInfo.getCurrentText() + "\n" +
						       "temperature in C: " + weatherInfo.getCurrentTempC() + "\n" +
					           "wind direction: " + weatherInfo.getWindDirection() + "\n" +
						       "wind speed: " + weatherInfo.getWindSpeed() + "\n" +
					           "Humidity: " + weatherInfo.getAtmosphereHumidity() + "\n" +
						       "Pressure: " + weatherInfo.getAtmospherePressure() + "\n" +
					           "Visibility: " + weatherInfo.getAtmosphereVisibility()
					           );
//			final ForecastInfo forecastInfo3 = weatherInfo.getForecastInfo3();
//			tvWeather0.setText("====== FORECAST 0 ======" + "\n" +
//			                   "date: " + forecastInfo3.getForecastDate() + "\n" +
//			                   "weather: " + forecastInfo3.getForecastText() + "\n" +
//					           "low  temperature in C: " + forecastInfo3.getForecastTempLowC() + "\n" +
//			                   "high temperature in C: " + forecastInfo3.getForecastTempHighC() + "\n" 
//					           );
			final ForecastInfo forecastInfo1 = weatherInfo.getForecastInfo1();
			tvWeather1.setText("====== FORECAST 1 ======" + "\n" +
			                   "date: " + forecastInfo1.getForecastDate() + "\n" +
			                   "weather: " + forecastInfo1.getForecastText() + "\n" +
					           "low  temperature in C: " + forecastInfo1.getForecastTempLowC() + "\n" +
			                   "high temperature in C: " + forecastInfo1.getForecastTempHighC() + "\n" 
					           );
			final ForecastInfo forecastInfo2 = weatherInfo.getForecastInfo2();
			tvWeather2.setText("====== FORECAST 2 ======" + "\n" +
					   "date: " + forecastInfo2.getForecastDate() + "\n" +
	                   "weather: " + forecastInfo2.getForecastText() + "\n" +
			           "low  temperature in C: " + forecastInfo2.getForecastTempLowC() + "\n" +
	                   "high temperature in C: " + forecastInfo2.getForecastTempHighC() + "\n" 
			           );
			
			LoadWebImagesTask task = new LoadWebImagesTask();
			task.execute(
					weatherInfo.getCurrentConditionIconURL(), 
					weatherInfo.getForecastInfo1().getForecastConditionIconURL(),
					weatherInfo.getForecastInfo2().getForecastConditionIconURL()
					);
        } else {
        	setNoResultLayout();
        }
	}
	
	private void setNormalLayout() {
		ivWeather0.setVisibility(View.VISIBLE);
		ivWeather1.setVisibility(View.VISIBLE);
		ivWeather2.setVisibility(View.VISIBLE);
		tvWeather0.setVisibility(View.VISIBLE);
		tvWeather1.setVisibility(View.VISIBLE);
		tvWeather2.setVisibility(View.VISIBLE);
		tvTitle.setVisibility(View.VISIBLE);
		tvErrorMessage.setVisibility(View.INVISIBLE);
	}
	
	private void setNoResultLayout() {
		ivWeather0.setVisibility(View.INVISIBLE);
		ivWeather1.setVisibility(View.INVISIBLE);
		ivWeather2.setVisibility(View.INVISIBLE);
		tvWeather0.setVisibility(View.INVISIBLE);
		tvWeather1.setVisibility(View.INVISIBLE);
		tvWeather2.setVisibility(View.INVISIBLE);
		tvTitle.setVisibility(View.INVISIBLE);
		tvErrorMessage.setVisibility(View.VISIBLE);
		tvErrorMessage.setText("Sorry, no result returned");
	}
	
	class LoadWebImagesTask extends AsyncTask<String, Void, Bitmap[]> {

		@Override
		protected Bitmap[] doInBackground(String... params) {
			Bitmap[] res = new Bitmap[3];
			res[0] = ImageUtils.getBitmapFromWeb(params[0]);
			res[1] = ImageUtils.getBitmapFromWeb(params[1]);
			res[2] = ImageUtils.getBitmapFromWeb(params[2]);
			return res;
		}

		@Override
		protected void onPostExecute(Bitmap[] results) {
			super.onPostExecute(results);
			ivWeather0.setImageBitmap(results[0]);
			ivWeather1.setImageBitmap(results[1]);
			ivWeather2.setImageBitmap(results[2]);
		}
		
	}
}
