package com.andro.routine.weather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andro.routine.HttpHandler;
import com.andro.routine.R;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final Integer LOCATION = 0x1;
    private static final String TAG = "WeatherFragment";
    private static String url = "https://api.darksky.net/forecast/fb108f81781321837473ec9565c058f7/%s,%s?units=auto";
    private static String[] weatherDataValues = {"city", "summary", "temperature", "humidity",
            "windSpeed", "pressure", "precipProbaility", "summaryDaily", "temperatureMin",
            "temperatureMax", "feelMin", "feelMax"};

    private FusedLocationProviderClient mFusedLocationClient;
    private SharedPreferences weatherCache;
    private SharedPreferences.Editor editor;
    private Geocoder geocoder;

    private SwipeRefreshLayout swipeLayout;
    private TextView darkySkyLink, city, summary, temperature, humidity, windSpeed, pressure,
            precipProbability, summaryDaily, temperatureMin, temperatureMax;
    private ImageView weatherIcon;

    private Double latitude, longitude;
    private Weather weather;

    public WeatherFragment() {
        // Required empty public constructor
    }

    public class RefreshThread extends Thread {
        @Override
        public void run() {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(R.string.title_weather);
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        weatherCache = getActivity().getSharedPreferences("WeatherData", MODE_PRIVATE);

        swipeLayout = view.findViewById(R.id.swipe_refresh_weather);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), android.R.color.holo_green_dark),
                ContextCompat.getColor(getContext(), android.R.color.holo_red_dark),
                ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark),
                ContextCompat.getColor(getContext(), android.R.color.holo_orange_dark));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        weather = new Weather();

        darkySkyLink = view.findViewById(R.id.dark_sky_text);
        city = view.findViewById(R.id.city);
        summary = view.findViewById(R.id.summary);
        temperature = view.findViewById(R.id.temperature);
        humidity = view.findViewById(R.id.humidity);
        windSpeed = view.findViewById(R.id.wind_speed);
        pressure = view.findViewById(R.id.pressure);
        precipProbability = view.findViewById(R.id.precip_probabillity);
        summaryDaily = view.findViewById(R.id.summary_daily);


        weatherIcon = view.findViewById(R.id.weather_icon);

        if (weatherCache.getBoolean("hasData", false)) {
            city.setText(weatherCache.getString(weatherDataValues[0], ""));
            summary.setText(weatherCache.getString(weatherDataValues[1], ""));
            temperature.setText(weatherCache.getString(weatherDataValues[2], ""));
            humidity.setText(weatherCache.getString(weatherDataValues[3], ""));
            windSpeed.setText(weatherCache.getString(weatherDataValues[4], ""));
            pressure.setText(weatherCache.getString(weatherDataValues[5], ""));
            precipProbability.setText(weatherCache.getString(weatherDataValues[6], ""));
            summaryDaily.setText(weatherCache.getString(weatherDataValues[7], ""));
            weatherIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cloud_sun));
        } else {
            onRefresh();
            swipeLayout.setRefreshing(true);
        }

        darkySkyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://darksky.net/poweredby/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return view;

    }

    private String getDate(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    private String getHumidity(String humidity) {
        Double humid = Double.parseDouble(humidity);
        humid *= 100;
        return Double.toString(Math.ceil(humid)) + "%";
    }

    @Override
    public void onRefresh() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION);
                onRefresh();
            }
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                url = String.format(url, latitude.toString(), longitude.toString());
                                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                Log.d(TAG, url);
                                if (checkConnection()) {
                                    new GetWeather().execute();
                                } else {
                                    Toast.makeText(getContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    private class GetWeather extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    editor = weatherCache.edit();

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject currently = jsonObj.getJSONObject("currently");
                    JSONObject daily = jsonObj.getJSONObject("daily");
                    JSONArray dailyData = daily.getJSONArray("data");

                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String cityName = addresses.get(0).getAddressLine(0);
                    String stateName = addresses.get(0).getAddressLine(1);

                    editor.putBoolean("hasData", true);

                    weather.setCity(cityName + ", " + stateName);
                    editor.putString(weatherDataValues[0], cityName + ", " + stateName);

                    weather.setSummary(currently.getString("summary"));
                    editor.putString(weatherDataValues[1], currently.getString("summary"));

                    weather.setTemperature(Double.toString(Math.ceil(Double.parseDouble(currently.getString("temperature")))) + "ºC");
                    editor.putString(weatherDataValues[2], Double.toString(Math.ceil(Double.parseDouble(currently.getString("temperature")))) + "ºC");

                    weather.setHumidity("Humidity : " + getHumidity(currently.getString("humidity")));
                    editor.putString(weatherDataValues[3], "Humidity : " + getHumidity(currently.getString("humidity")));

                    weather.setWindSpeed("Wind Speed : " + currently.getString("windSpeed") + " km/h");
                    editor.putString(weatherDataValues[4], "Wind Speed : " + currently.getString("windSpeed") + " km/h");

                    weather.setPressure("Pressure : " + Double.toString(Math.ceil(Double.parseDouble(currently.getString("pressure")))) + " mb");
                    editor.putString(weatherDataValues[5], "Pressure : " + Double.toString(Math.ceil(Double.parseDouble(currently.getString("pressure")))));

                    weather.setPrecipProbability("Precipitation Probability : " + getHumidity(currently.getString("precipProbability")));
                    editor.putString(weatherDataValues[6], "Precipitation Probability : " + getHumidity(currently.getString("precipProbability")));

                    weather.setSummaryDaily("Weekly Forecast : " + daily.getString("summary"));
                    editor.putString(weatherDataValues[7], "Weekly Forecast : " + daily.getString("summary"));

                    weather.setTime(getDate(Long.parseLong(currently.getString("time"))));

                    editor.apply();
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            swipeLayout.setRefreshing(false);

            city.setText(weather.getCity());
            summary.setText(weather.getSummary());
            temperature.setText(weather.getTemperature());
            windSpeed.setText(weather.getWindSpeed());
            pressure.setText(weather.getPressure());
            humidity.setText(weather.getHumidity());
            precipProbability.setText(weather.getPrecipProbability());
            summaryDaily.setText(weather.getSummaryDaily());
            weatherIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cloud_sun));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && (
                activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                        activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE ||
                        activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET);
    }
}
