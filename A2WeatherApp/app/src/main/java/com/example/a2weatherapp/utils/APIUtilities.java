package com.example.a2weatherapp.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.a2weatherapp.MainActivity;
import com.example.a2weatherapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class APIUtilities {

    private static String API = "581f950626997324381818bd5dab2ff0";
    private static String APIcaller = "https://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s";

    public static class OpenWeather extends AsyncTask<String, Void, String> {

        private final String locationSet;

        @SuppressLint("StaticFieldLeak")
        private final MainActivity mainActivity;

        private double temp;
        private String weatherStatus;
        private double minTemp;
        private double maxTemp;
        private int sunriseTime;
        private int sunsetTime;
        private int windSpeed;
        private int humidity;
        private int precipitation;

        public OpenWeather(MainActivity mainActivity, String locationSet) {
            this.locationSet = locationSet;
            this.mainActivity = mainActivity;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null && result.equals("ERROR_FOUND")) {
                ((TextView) mainActivity.findViewById(R.id.address)).setText(String.format(mainActivity.getString(R.string.city_name), "INVALID CITY"));
                return;
            }

            ((TextView) mainActivity.findViewById(R.id.address)).setText(String.format(mainActivity.getString(R.string.city_name), locationSet));

            ((TextView) mainActivity.findViewById(R.id.status)).setText(weatherStatus);

            ((TextView) mainActivity.findViewById(R.id.temp)).setText(String.format(mainActivity.getString(R.string.temperature), convertKelvinsToCelsius(temp)));

            ((TextView) mainActivity.findViewById(R.id.temp_min)).setText(String.format(mainActivity.getString(R.string.min_temp), convertKelvinsToCelsius(minTemp)));
            ((TextView) mainActivity.findViewById(R.id.temp_max)).setText(String.format(mainActivity.getString(R.string.max_temp), convertKelvinsToCelsius(maxTemp)));

            ((TextView) mainActivity.findViewById(R.id.humidity)).setText(String.format(mainActivity.getString(R.string.precipitation), humidity + ""));
            ((TextView) mainActivity.findViewById(R.id.precipitation)).setText(String.format(mainActivity.getString(R.string.precipitation), precipitation + ""));

            ((TextView) mainActivity.findViewById(R.id.sunrise)).setText(convertUTCToLocal(sunriseTime));
            ((TextView) mainActivity.findViewById(R.id.sunset)).setText(convertUTCToLocal(sunsetTime));

            ((TextView) mainActivity.findViewById(R.id.wind)).setText(String.format(mainActivity.getString(R.string.wind_speed), windSpeed));

            // Update the updated time
            ((TextView) mainActivity.findViewById(R.id.updated_at)).setText(String.format(mainActivity.getString(R.string.updated_at), Calendar.getInstance().getTime().toString()));

        }

        private String convertKelvinsToCelsius(double kelvin) {
            return new DecimalFormat("#.#").format((kelvin - 273.15));
        }

        private String convertUTCToLocal(int time) {
            Instant instant = Instant.ofEpochSecond(time) ;
            return Date.from(instant).toString();
        }

        @Override
        protected String doInBackground(String... str) {
            try {
                final URL url = new URL(String.format(APIcaller, locationSet, API));
                final StringBuilder rawJson = new StringBuilder();
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                try (final BufferedReader reader =
                             new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    rawJson.append(reader.readLine());
                } catch (Exception e) {
                    return "ERROR_FOUND";
                }

                try {
                    JSONObject allInfo = new JSONObject(rawJson.toString());
                    JSONArray weatherGeneral = allInfo.getJSONArray("weather");
                    JSONObject weatherMain = allInfo.getJSONObject("main");
                    JSONObject weatherSys = allInfo.getJSONObject("sys");
                    JSONObject weatherWind = allInfo.getJSONObject("wind");
                    try {
                        JSONObject weatherRain = allInfo.getJSONObject("rain");
                        this.precipitation = (int) (weatherRain.getDouble("1h") * 100);
                    } catch (JSONException e) {
                        Log.e("A2", "No rain node found, next.");
                    }

                    this.weatherStatus = ((JSONObject) weatherGeneral.get(0)).getString("description");

                    this.temp = weatherMain.getDouble("temp");
                    this.minTemp = weatherMain.getDouble("temp_min");
                    this.maxTemp = weatherMain.getDouble("temp_max");
                    this.humidity = weatherMain.getInt("humidity");

                    this.sunriseTime = weatherSys.getInt("sunrise");
                    this.sunsetTime = weatherSys.getInt("sunset");

                    this.windSpeed = weatherWind.getInt("speed");

                    Log.e("A2", rawJson.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
