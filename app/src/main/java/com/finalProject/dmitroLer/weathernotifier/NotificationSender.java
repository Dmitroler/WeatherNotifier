package com.finalProject.dmitroLer.weathernotifier;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.finalProject.dmitroLer.weathernotifier.items.WeatherUpdateItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NotificationSender extends IntentService {

    List<WeatherUpdateItem> weatherUpdateItems = new ArrayList<>();

    private static final String TAG = "NotificationSender";
    private Address currentAddress;
    int numberOfSuccesses = 0;
    private int numberOfAddresses;
    private NotificationCompat.InboxStyle inboxStyle;
    private Intent notifyIntent;
    private NotificationCompat.Builder mBuilder;
    private boolean userSelectedOptionsApply = false;

    private boolean probabilityFlag = false;
    private boolean temperatureFlag = false;
    private boolean humidtyflag = false;
    private boolean uvFlag = false;
    private boolean windFlag = false;

    private double probabilityMax=0;
    private double temperatureMax=0;
    private double humidtyMax=0;
    private double uvMax=0;
    private double windMax=0;

    private String probabilityLocation = "";
    private String temperatureLocation = "";
    private String humidityLocation = "";
    private String uvLocation = "";
    private String windLocation = "";

    public NotificationSender() {
        super("NotificationSender");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {


        mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_misc_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("New weather update")
                .setContentText("New weather update available");

        notifyIntent = new Intent(Intent.makeMainActivity(new ComponentName(this, WeatherUpdateActivity.class)));


        final Context context = this;

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Type stringAddressHashMap = new TypeToken<HashMap<String, Address>>() {
        }.getType();
        HashMap < String, Address > addressHashMap;
        addressHashMap = (new Gson()).fromJson(intent != null ? intent.getStringExtra(Constants.ADDRESSES_HASH_MAP) : null, stringAddressHashMap);
        numberOfAddresses = addressHashMap != null ? addressHashMap.size() + 1 : 1;
        inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Weather update:");
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            try {
                                currentAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                handleAddress(currentAddress);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, "onSuccess: No address received from geocoder");
                            }
                        } else {
                            Log.d(TAG, "onSuccess: No location found");
                            if (--numberOfAddresses == numberOfSuccesses)
                                sendNotification();
                        }
                    }
                });
        assert addressHashMap != null;
        for (String addressKey : addressHashMap.keySet()) {
            Address address = addressHashMap.get(addressKey);
            handleAddress(address);
        }
    }

    private void handleAddress(final Address address) {
        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(address.getLatitude()));
        request.setLng(String.valueOf(address.getLongitude()));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);
        Log.d(TAG, "weatherRequestBuilder: request");
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
                WeatherUpdateItem item = new WeatherUpdateItem(String.valueOf(numberOfSuccesses), weatherResponse, address.getLocality());
                weatherUpdateItems.add(item);
                String toNotification = stringToNotification(item);
                if (toNotification != null) {
                    inboxStyle.addLine("UV in Tel Aviv-Yafo is 11");
                    if(probabilityFlag){
                        inboxStyle.addLine("Probability for precipitation in" + probabilityLocation + " " + probabilityMax*100 );
                        inboxStyle.addLine("An umbrella will be required later in the day");
                        probabilityFlag=false;
                        probabilityMax=0;
                    }
                    if(temperatureFlag){
                        inboxStyle.addLine("Temprature in "+ temperatureLocation + " " + temperatureMax);
                        temperatureFlag=false;
                        temperatureMax=0;
                    }
                    if(humidtyflag){
                        inboxStyle.addLine("Humidty in "+ humidityLocation + " " + humidtyMax);
                        humidtyflag=false;
                        humidtyMax=0;
                    }
                    if(uvFlag){
                        inboxStyle.addLine("UV in "+ uvLocation + " " + uvMax);
                        if(sharedPreferences.getInt(Constants.EYE_COLOR,1)>2)
                        inboxStyle.addLine("High risk of UV rays, use sunglasses and sunscreen");
                        else
                            inboxStyle.addLine("High risk of UV rays, use sunscreen");
                        uvFlag=false;
                        uvMax=0;
                    }
                    if(windFlag){
                        inboxStyle.addLine("Wind in "+ windLocation + " " + windMax);
                        windFlag=false;
                        windMax=0;
                    }

                    userSelectedOptionsApply = true;
                }
                if (++numberOfSuccesses == numberOfAddresses) {
                    sendNotification();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG, "Error while calling: " + retrofitError.getUrl());
            }
        });
    }

    private void sendNotification() {
        mBuilder.setStyle(inboxStyle);
        notifyIntent.putExtra(Constants.WEATHER_UPDATE_ITEMS, (new Gson()).toJson(weatherUpdateItems));
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Sends a notification only if there is an anomaly in one of the criteria
        // the user choose in one of the locations the user choose
        if (userSelectedOptionsApply) {
            notificationManager.notify(0, mBuilder.build());
        }
    }

    private String stringToNotification(WeatherUpdateItem item) {

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);

        String message = "";

        double probabilityTempMax = 0;
        double temperatureTempMax = 0;
        double humidtyTempMax = 0;
        double uvTempMax = 0;
        double windTempMax = 0;


        boolean optionUV = sharedPreferences.getBoolean(Constants.OPTION_UV, true);
        boolean optionTemprature = sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true);
        boolean optionWind = sharedPreferences.getBoolean(Constants.OPTION_WIND, true);
        boolean optionHumidity = sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true);
        boolean optionRain = sharedPreferences.getBoolean(Constants.OPTION_RAIN, true);

        for (int i = 1; i < 6; i++) {

            if (optionRain) {
                probabilityTempMax = Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getPrecipProbability());
                if (probabilityTempMax > 0.6 && probabilityTempMax > probabilityMax) {
                    probabilityMax = probabilityTempMax;
                    probabilityFlag = true;
                    probabilityLocation = "" + item.location;
                   // String precipitation = String.valueOf(Double.valueOf(item.weatherResponse.getCurrently().getPrecipProbability()) * 100);
                   // precipitation = precipitation.substring(0, precipitation.indexOf(".")) + Constants.PERCENT;
                   // message = message + "Probability for " + item.weatherResponse.getHourly().getData().get(i).getPrecipType() + " in " + item.location + ": " + precipitation ;
                }
            }

            if (optionTemprature) {
                temperatureTempMax = item.weatherResponse.getHourly().getData().get(i).getTemperature();
                if (temperatureTempMax> 23 && temperatureTempMax > temperatureMax) {
                    temperatureMax = temperatureTempMax;
                    temperatureFlag = true;
                    temperatureLocation = "" + item.location;

                   // String temprature = (int) item.weatherResponse.getHourly().getData().get(i).getTemperature() + Constants.DEGREE;
                  //  message= message + "Temprature in "+ item.location + ": " + temprature +"\n";
                }
            }

            if (optionHumidity) {
                humidtyTempMax = Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getHumidity());
                if (humidtyTempMax > 0.75 && humidtyTempMax>humidtyMax) {
                    humidtyMax = humidtyTempMax;
                    humidtyflag = true;
                    humidityLocation = "" + item.location;

                  //  String humidity = (int) (Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getHumidity()) * 100) + Constants.PERCENT;
                   // message "Humidity in " + item.location + ": " + humidity;
                }
            }

            if (optionUV) {
                uvTempMax=  Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getHumidity());

                if (uvTempMax > 9) {
                    uvMax = uvTempMax;
                    uvFlag = true;
                    uvLocation = "" + item.location;

                  //  String humidity = (int) (Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getHumidity()) * 100) + Constants.PERCENT;
                   // return "Humidity in " + item.location + ": " + humidity;
                }
            }

            if (optionWind) {
                windTempMax = Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getWindSpeed());
                if (windTempMax > 35) {
                    windMax = windTempMax;
                    windFlag = true;
                    windLocation = "" + item.location;

                  //  String wind = String.valueOf((int) (Double.valueOf(item.weatherResponse.getHourly().getData().get(i).getWindSpeed()) * 1.609));
                  //  return "Wind speed in " + item.location + ": " + wind;
                }
            }

        }

        return message;

    }
}
