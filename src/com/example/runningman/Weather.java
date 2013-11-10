package com.example.runningman;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.weather.tools.AsciiUtils;
import com.example.weather.utils.YahooWeather4a.WeatherInfo;
import com.example.weather.utils.YahooWeather4a.WeatherInfo.ForecastInfo;
import com.example.weather.utils.YahooWeather4a.YahooWeatherInfoListener;
import com.example.weather.utils.YahooWeather4a.YahooWeatherUtils;

public class Weather implements YahooWeatherInfoListener {
	private YahooWeatherUtils yahooWeatherUtils = YahooWeatherUtils.getInstance();
    private String location = "Singapore";
    private DBInterface DBI;
    // define the policy of poor weather condition
    public String[] poorConditionArray = new String[] {
		"tornado",
		"storm",
		"hurricane",
		"thunderstorms",
		"rain",
		"snow",
		"drizzle",
		"showers",
		"hail",
		"sleet",
		"dust",
		"foggy",
		"haze",
		"smoky",
		"blustery",
		"thundershowers",
	};
    
    public Weather(Context context) {
    	String convertedlocation = AsciiUtils.convertNonAscii(location);
        yahooWeatherUtils.queryYahooWeather(context, convertedlocation, this);
        DBI = new DBInterface(context);
    }

	@Override
	public void gotWeatherInfo(WeatherInfo weatherInfo) {
		// remove the old entries for they are out-dated.
		DBI.delete(DBI.tableWeather, null);
		this.updateWeatherDB(weatherInfo);
	}
	
	private void insertWeatheDB(String date, String weather, int temp) {
		try {
        	String newDateString = new SimpleDateFormat("yyyy-MM-dd",Locale.US)
        		.format(new SimpleDateFormat("dd MMM yyyy",Locale.US).parse(date));
        	Log.d("date", newDateString);
			Log.d("weather", weather);
        	Log.d("temp", Integer.toString(temp));
        	Log.d("weather condition", Boolean.toString(this.isWeatherPoorCondition(weather)));
        	
        	ContentValues CV = new ContentValues();
			CV.put("Date", newDateString);
			CV.put("Weather", weather);
			CV.put("Temperature", temp);
			DBI.insert(DBI.tableWeather, CV);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void updateWeatherDB(WeatherInfo weatherInfo) {
		if(weatherInfo != null) {
			// do not need forecast 1 which is today
			ForecastInfo forecastInfo2 = weatherInfo.getForecastInfo2();
			
			String currDateString = weatherInfo.getCurrentConditionDate().substring(5, 16);
			String currWeather = weatherInfo.getCurrentText();
			int currTemp = weatherInfo.getCurrentTempC();
			this.insertWeatheDB(currDateString, currWeather, currTemp);
			
			String forecast2Date = forecastInfo2.getForecastDate();
			String forecast2Weather = forecastInfo2.getForecastText();
			int forecast2Temp = forecastInfo2.getForecastTempHighC();
			this.insertWeatheDB(forecast2Date, forecast2Weather, forecast2Temp);
		}
	}
	
	public boolean isWeatherPoorCondition(String weatherText) {
		boolean result = false;
		for (int i = 0; i < this.poorConditionArray.length; i++) {
			if(weatherText.toLowerCase().contains(this.poorConditionArray[i])){
				result = true;
				return result;
			}
		}
		return result;
	}
    
	/*
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


	// Set up the {@link android.app.ActionBar}.

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
	*/    	
	
	/*
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
        */
	
	/*
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
	*/
}
