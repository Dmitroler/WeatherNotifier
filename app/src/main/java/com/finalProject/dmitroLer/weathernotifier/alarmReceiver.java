package com.finalProject.dmitroLer.weathernotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.finalProject.dmitroLer.weathernotifier.items.WeatherUpdateItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

public class alarmReceiver extends BroadcastReceiver {
    private static final String TAG = "Alarm Receiver";
    private NotificationCompat.Builder mBuilder;
    private HashMap<String, Address> addressHashMap;
    private List<WeatherUpdateItem> dailyWeatherItems;
    private int numberOfSuccesses = 0;
    private int numberOfAddresses;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive: Alarm received");
        addressesHashMapSetup(context);
        dailyWeatherItems = new ArrayList<>();
        mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_misc_notification)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Daily weather update")
                .setContentText("Daily weather update available");

        numberOfAddresses++;
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            try {
                                Address currentAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                handleAddress(currentAddress, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            if (--numberOfAddresses==numberOfSuccesses){
                                sendNotification(context);
                            }
                        }
                    }
                });
        for (String key : addressHashMap.keySet()){
            handleAddress(addressHashMap.get(key),context);
        }
    }

    private void addressesHashMapSetup(Context context) {
        addressHashMap = new HashMap<>();
        SharedPreferences addressesSharedPreferences = context.getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        numberOfAddresses = addressesSharedPreferences.getInt(Constants.NUMBER_OF_ADDRESSES, 0);
        for (int i = 0; i < numberOfAddresses; i++) {
            addressHashMap.put(
                    String.valueOf(i + 1)
                    , PublicMethods.getSavedObjectFromPreference(context, Constants.ADDRESSES_PREFERENCE, String.valueOf(i + 1), Address.class)
            );
        }
    }

    private void handleAddress(final Address address, final Context context) {

        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(address.getLatitude()));
        request.setLng(String.valueOf(address.getLongitude()));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);
        Log.d(TAG, "handleAddress: request");
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                dailyWeatherItems.add(new WeatherUpdateItem(String.valueOf(numberOfSuccesses), weatherResponse, address.getLocality()));
                if (++numberOfSuccesses == numberOfAddresses) {
                    sendNotification(context);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG, "Error while calling: " + retrofitError.getUrl());
            }
        });
    }

    private void sendNotification(Context context) {

        Intent intent = new Intent(context,DailyWeatherUpdateActivity.class);
        intent.putExtra(Constants.DAILY_WEATHER_ITEMS,(new Gson()).toJson(dailyWeatherItems));
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }
}
