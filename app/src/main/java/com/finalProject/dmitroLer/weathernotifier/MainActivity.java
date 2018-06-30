
package com.finalProject.dmitroLer.weathernotifier;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.finalProject.dmitroLer.weathernotifier.items.RecyclerItems;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.gson.Gson;
import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements
        NotificationSettingsFragment.onUpdateTimeSelectedListener
        , LocationsFragment.OnListFragmentInteractionListener, SplashFragment.OnFragmentTimeOutListener {



    private Address mAddress;
    Fragment mFragment;
    HashMap<String, Address> addressHashMap = new HashMap<>();
    Calendar calendar = Calendar.getInstance();
    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;

    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    private GoogleApiClient mApiClient;

    private boolean activityIsActive = true;
    private AlarmManager alarmManager;
    private MainFragment mainFragment;
    private NotificationSettingsFragment notificationSettingsFragment;
    private BluetoothFragment bluetoothFragment;
    private LocationsFragment locationsFragment;
    private Stack<String> mFragmentStack;
    private boolean backWasPressed;
    private int[] updateTimeMillis;
    private int apparent= calendar.get(Calendar.HOUR_OF_DAY);
    private Thread locationUpdateThread;
    private boolean firstUpdate = true;
    private SplashFragment splashFragment;
    private boolean connectionOngoing = true;
    private boolean removeSplashOnResume;
    private AlertDialog timeOutAlertDialog;
    private boolean notificationThreadNotActive = true;
    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(Constants.ALERT_DILOGE_INTENT)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    if(ConnectionService.titlee.equals("NOTIFICATION")){
                        {builder.setTitle("Please make sure that the device is turn on and in range");}
                      }else {builder.setTitle(ConnectionService.titlee);}

                            builder.setMessage(ConnectionService.infoo)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // sendMonitorData();
                                    if(ConnectionService.titlee.equals("Sensor Covered")){
                                        sendBroadcast(new Intent(Constants.STOP_N_ALARAM_TONE));
                                    }
                                    else {
                                        sendBroadcast(new Intent(Constants.STOP_N_ALARAM_TONE));
                                        sendBroadcast(new Intent(Constants.SEND_MONITOR_DATA_INTENT));
                                    }
                                    if(ConnectionService.titlee.equals("ALARM - UVDetection")){
                                        sendBroadcast(new Intent(Constants.RESET_VALUES_INTENT));
                                    }

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    Constants.isToShowAlert = false;
            }
        }
    } ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.finalProject.dmitroLer.weathernotifier.R.layout.activity_main);
        ForecastApi.create(Constants.API_KEY2);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mFragmentStack = new Stack<>();

        mainFragment = new MainFragment();
        splashFragment = new SplashFragment();

        if (findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            setupMainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(com.finalProject.dmitroLer.weathernotifier.R.id.fragment_container, splashFragment, SplashFragment.class.getName());
            transaction.commit();
        }

        notificationSettingsFragment = new NotificationSettingsFragment();
        locationsFragment = new LocationsFragment();
        bluetoothFragment = new BluetoothFragment();

        mResultReceiver = new AddressResultReceiver(new Handler());

        // for data saving and loading
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);

        //
        addressesHashMapSetup();
        if (!sharedPreferences.getBoolean(Constants.EXIT_WITH_BACK_BUTTON, false))
            recyclerViewSetup();
        sharedPreferences.edit().putBoolean(Constants.EXIT_WITH_BACK_BUTTON, false).apply();

        updateTimeMillis = getResources().getIntArray(com.finalProject.dmitroLer.weathernotifier.R.array.update_times_millis);
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .enableAutoManage(this, 1, null)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                        Log.d(TAG, "onConnected: Awareness connected");

                        locationUpdateThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (connectionOngoing) {
                                    updateLocation();
                                    try {
                                        Thread.sleep(15000);
                                    } catch (InterruptedException e) {
                                        Log.d(TAG, "run: sleep failed");
                                        e.printStackTrace();
                                    }
                                }
                            }

                        });
                        locationUpdateThread.start();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(Constants.ALERT_DILOGE_INTENT);
        registerReceiver(mReceiver,intentFilter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Permission granted");
                    updateLocation();
                } else {
                    Toast.makeText(this, Constants.APPLICATION_NEEDS_PERMISSION, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void updateLocation() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "updateLocation: Location request sent");
            final Context context = this;
            Awareness.SnapshotApi.getLocation(mApiClient).setResultCallback(new ResultCallback<LocationResult>() {
                @Override
                public void onResult(@NonNull LocationResult locationResult) {
                    mLastLocation = locationResult.getLocation();
                    Log.d(TAG, "onResult: Location updated");
                    if (activityIsActive) {
                        if (mLastLocation != null) {
                            startFetchAddressIntentService();
                        } else {
                            Log.d(TAG, "onResult: unable to get location");
                            String lastLocationString = sharedPreferences.getString(Constants.LAST_KNOWN_LOCATION, null);
                            if (!(lastLocationString != null && lastLocationString.equals("null"))) {
                                Log.d(TAG, "onResult: "+ lastLocationString);
                              //  Toast.makeText(context, "Using last known location", Toast.LENGTH_SHORT).show();
                                mLastLocation = (new Gson()).fromJson(lastLocationString, Location.class);
                                startFetchAddressIntentService();
                            }else{
                                Log.d(TAG, "onResult: No last known location");
                            }
                        }
                    }
                }
            });
        }
    }

    private void FetchWeatherResponse() {
        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(mAddress.getLatitude()));
        request.setLng(String.valueOf(mAddress.getLongitude()));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);
        Log.d(TAG, "FetchWeatherResponse: Weather request sent");
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                TextView apparentt;
                apparentt = (TextView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.apparent_temperature_value);
                Log.d(TAG, "success: received weather response");
                String temp = String.valueOf(weatherResponse.getCurrently().getTemperature());
                temp = temp.substring(0, temp.indexOf(".") + 2) + Constants.DEGREE;
                ((TextView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.temperature_value)).setText(temp);
                String app_temp = String.valueOf(weatherResponse.getCurrently().getApparentTemperature());
                app_temp = app_temp.substring(0, temp.indexOf(".") + 2) + Constants.DEGREE;
                String humidity = (int) (Double.valueOf(weatherResponse.getCurrently().getHumidity()) * 100) + Constants.PERCENT;
                ((TextView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.Humidity_value)).setText(humidity);

                if(apparent==1)
                    apparentt.setText(""+(apparent-1));
                if(apparent==2)
                    apparentt.setText(""+(apparent-2));
                if(apparent==7)
                    apparentt.setText(""+(apparent-6));
                if(apparent==8)
                    apparentt.setText(""+(apparent-6));
                if(apparent==9)
                    apparentt.setText(""+(apparent-7));
                if(apparent==10)
                    apparentt.setText(""+(apparent-5));
                if(apparent==11)
                    apparentt.setText(""+(apparent-3));
                if(apparent==12)
                    apparentt.setText(""+(apparent-1));
                if(apparent==13)
                    apparentt.setText(""+(apparent-1));
                if(apparent==14)
                    apparentt.setText(""+(apparent-3));
                if(apparent==15)
                    apparentt.setText(""+(apparent-7));
                if(apparent==16)
                    apparentt.setText(""+(apparent-11));
                if(apparent==17)
                    apparentt.setText(""+(apparent-14));
                if(apparent==18)
                    apparentt.setText(""+(apparent-16));
                if(apparent==19)
                    apparentt.setText(""+(apparent-18));
                if(apparent==20 || apparent==21 || apparent==22 || apparent==23 || apparent==24)
                    apparentt.setText("0");

                String preciptation =  (String.valueOf(weatherResponse.getCurrently().getPrecipProbability())) + Constants.PERCENT;
                ((TextView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.precip_probability_value)).setText(preciptation);

                String wind = String.valueOf(Double.valueOf(weatherResponse.getCurrently().getWindSpeed()) * 1.609);
                wind = wind.substring(0, wind.indexOf(".") + 2);
                ((TextView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.wind_speed_value)).setText(wind);

                PublicMethods.changeIcon(weatherResponse.getCurrently().getIcon(), (ImageView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.current_icon), true);

                handleForecast(weatherResponse);

                //removes splash screen
                if (firstUpdate) {
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (activityIsActive) {
                                Toolbar toolbar = (Toolbar) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.toolbar);
                                setSupportActionBar(toolbar);
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                                            transaction.setCustomAnimations(R.anim.grow_from_middle, R.anim.shrink_to_middle);
                                transaction.replace(com.finalProject.dmitroLer.weathernotifier.R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
                                transaction.commit();
                                findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.app_bar).setVisibility(View.VISIBLE);
                                if (timeOutAlertDialog != null && timeOutAlertDialog.isShowing())
                                    timeOutAlertDialog.dismiss();
                            } else {
                                removeSplashOnResume = true;
                            }
                            splashFragment.cancelTimeOutTimer();
                            firstUpdate = false;
                        }
                    }, 1000);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure: ");
//                                Toast.makeText(context, "Can't connect to dark-sky", Toast.LENGTH_LONG).show();
//                                finish();
            }
        });
    }

    private void setupMainFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(com.finalProject.dmitroLer.weathernotifier.R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
        transaction.addToBackStack(mainFragment.getClass().getName());
        mFragmentStack.add(mainFragment.getClass().getName());
        transaction.commit();
    }

    private void handleForecast(WeatherResponse weatherResponse) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 0);

        int forecastItemsHandled = 0;
        LinearLayout forecastLayout = (LinearLayout) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.forecast_layout);
        for (int i = 0; i < forecastLayout.getChildCount(); i++) {
            if (forecastLayout.getChildAt(i) instanceof LinearLayout) {
                LinearLayout forecastItem = (LinearLayout) forecastLayout.getChildAt(i);
                forecastItemsHandled++;
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);

                ((TextView) forecastItem.getChildAt(0))
                        .setText(DateUtils.formatDateTime(this, calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME)
                        );

                PublicMethods.changeIcon(weatherResponse.getHourly().getData().get(forecastItemsHandled).getIcon()
                        , (ImageView) forecastItem.getChildAt(1), false);

                String s = String.valueOf(weatherResponse.getHourly().getData().get(forecastItemsHandled).getTemperature());
                s = s.substring(0, s.indexOf(".") + 2) + Constants.DEGREE;
                ((TextView) forecastItem.getChildAt(2)).setText(s);
            }
        }
    }


    private void recyclerViewSetup() {
        for (int i = 1; i < addressHashMap.size() + 1; i++) {
            String key = String.valueOf(i);
            new RecyclerItems.RecyclerItem(key, addressHashMap.get(key).getLocality());
        }
    }

    private void addressesHashMapSetup() {
        SharedPreferences addressesSharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        int numberOfAddresses = addressesSharedPreferences.getInt(Constants.NUMBER_OF_ADDRESSES, 0);
        for (int i = 0; i < numberOfAddresses; i++) {
            addressHashMap.put(
                    String.valueOf(i + 1)
                    , PublicMethods.getSavedObjectFromPreference(this, Constants.ADDRESSES_PREFERENCE, String.valueOf(i + 1), Address.class)
            );
        }
    }

    @Override
    public void onFragmentTimeOut() {
        final Context context = this;
        connectionOngoing = false;
        runOnUiThread(new Runnable() {
            public void run() {
                timeOutAlertDialog = new AlertDialog.Builder(context).create();
                timeOutAlertDialog.setTitle("Connection timed out");
                timeOutAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        splashFragment.startTimeOutTimer(context);
                        connectionOngoing = true;
                        locationUpdateThread.start();
                    }
                });
                timeOutAlertDialog.setCancelable(false);
                timeOutAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                timeOutAlertDialog.show();
                splashFragment.stopLoadingAnimation();
            }
        });
    }

    @Override
    public void onUpdateTimeSelected() {
        Log.d(TAG, "onUpdateTimeSelected: Update time selected");
        notificationThreadNotActive = true;
    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the currentAddress string
            // or an error message sent from the intent service.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "onReceiveResult: Address received");
                switch (resultData.getInt(Constants.RECEIVE_TYPE_EXTRA)) {
                    case Constants.RECEIVE_TO_MAIN:
                        Address newAddress = resultData.getParcelable("currentAddress");
                        if (firstUpdate) {
                            mAddress = newAddress;
                            FetchWeatherResponse();
                        } else {
                            if ((newAddress.getLocality() != null && !newAddress.getLocality().matches(mAddress.getLocality()))) {
                                FetchWeatherResponse();
                            }
                            mAddress = newAddress;
                        }
                        displayAddressOutput(mAddress.getLocality() != null ? mAddress.getLocality() : "Current location");
                        break;
                    case Constants.RECEIVE_TO_FRAGMENT:
                        Address address = resultData.getParcelable("currentAddress");
                        addressHashMap.put(String.valueOf(RecyclerItems.ITEMS.size() + 1), address);
                        new RecyclerItems.RecyclerItem(
                                String.valueOf(RecyclerItems.ITEMS.size() + 1),
                                address.getLocality()
                        );
                        locationsFragment.getRecyclerViewAdapter().notifyItemInserted(RecyclerItems.ITEMS.size());
                        break;
                }
            } else {
                if (resultData.getInt(Constants.RECEIVE_TYPE_EXTRA) == Constants.RECEIVE_TO_FRAGMENT) {
                    Toast.makeText(getApplicationContext(), "No address found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (notificationThreadNotActive && !firstUpdate) {
            notificationThreadSetup();
        }
        saveAddressesToPreference();
        activityIsActive = false;

        //If the application is in background, resets fragments.
        //will not work if phone takes to long to move application to background.
        if (isApplicationInBackground()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(MainFragment.class.getName(), 0);
            mFragmentStack.clear();
            mFragmentStack.add(mainFragment.getClass().getName());
        }
        sharedPreferences.edit().putString(Constants.LAST_KNOWN_LOCATION, (new Gson()).toJson(mLastLocation)).apply();
        Constants.isVisible=false;

    }


    private void notificationThreadSetup() {
        notificationThreadNotActive = false;
        final Intent intent = new Intent(this, NotificationSender.class);
        final Context context = this;
        Thread notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    notificationThreadNotActive = true;
                    return;
                }
                Log.d(TAG, "run: NotificationThread is running");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isApplicationInBackground()) {
                    while (!notificationThreadNotActive) {
                        Log.d(TAG, "run: starting NotificationSender service");
                        intent.putExtra(Constants.ADDRESSES_HASH_MAP, (new Gson()).toJson(addressHashMap));
                        startService(intent);
                        try {
                            Thread.sleep(updateTimeMillis[sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION, 0)]);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    notificationThreadNotActive = true;
                }
            }
        });
        notificationThread.start();
    }

    private boolean isApplicationInBackground() {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            final ComponentName topActivity = tasks.get(0).topActivity;
            return !topActivity.getPackageName().equals(getPackageName());
        }
        return false;
    }
    private void saveAddressesToPreference() {
        int numberOfAddresses = addressHashMap.size();
        SharedPreferences addressesSharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = addressesSharedPreferences.edit();
        editor.clear().commit();
        for (int i = 0; i < numberOfAddresses; i++) {
            PublicMethods.saveObjectToSharedPreference(
                    this
                    , Constants.ADDRESSES_PREFERENCE
                    , String.valueOf(i + 1)
                    , addressHashMap.get(String.valueOf(i + 1))
            );
        }
        editor.putInt(Constants.NUMBER_OF_ADDRESSES, numberOfAddresses).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Constants.isToShowAlert) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(ConnectionService.titlee)
                    .setMessage(ConnectionService.infoo)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(ConnectionService.titlee.equals("Sensor Covered")){
                                return;
                            }
                           ConnectionService.write("7#".getBytes());
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            Constants.isToShowAlert = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (removeSplashOnResume) {
            removeSplashOnResume = false;
            Toolbar toolbar = (Toolbar) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.toolbar);
            setSupportActionBar(toolbar);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(com.finalProject.dmitroLer.weathernotifier.R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
            transaction.commit();
            findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.app_bar).setVisibility(View.VISIBLE);
        }
        activityIsActive = true;
        Constants.isVisible=true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Location resultLocation = data.getExtras().getParcelable(Constants.NEW_LOCATION);
            if (resultLocation != null) {
                Intent intent = new Intent(this, FetchAddressIntentService.class);
                intent.putExtra(Constants.RECEIVER, mResultReceiver);
                intent.putExtra(Constants.LOCATION_DATA_EXTRA, resultLocation);
                intent.putExtra(Constants.RECEIVE_TYPE_EXTRA, Constants.RECEIVE_TO_FRAGMENT);
                startService(intent);
            } else {
                Toast.makeText(this, Constants.NO_LOCATION_SELECTED, Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == Constants.FAILURE_RESULT) {
            Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.finalProject.dmitroLer.weathernotifier.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == com.finalProject.dmitroLer.weathernotifier.R.id.bluetooth) {
            switchFragments(bluetoothFragment);
            return true;
        }

        if (id == com.finalProject.dmitroLer.weathernotifier.R.id.settings) {
            switchFragments(notificationSettingsFragment);
            return true;
        }
        if (id == com.finalProject.dmitroLer.weathernotifier.R.id.define_location) {
            switchFragments(locationsFragment);
            return true;
        }
        if (id == com.finalProject.dmitroLer.weathernotifier.R.id.set_alarm_time) {
            makeAlarmDialogBox();
            return true;
        }
     /*   //  For Debugging use
       if (id == R.id.alarm_demo) {
            Intent intent = new Intent(this, alarmReceiver.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis(), alarmPendingIntent);
        }
        if (id == R.id.update_demo) {
            Intent intent = new Intent(this, NotificationSender.class);
            intent.putExtra(Constants.ADDRESSES_HASH_MAP, (new Gson()).toJson(addressHashMap));
            startService(intent);
        }

*/
        return super.onOptionsItemSelected(item);
    }

    private void makeAlarmDialogBox() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Constants.SET_ALARM_TIME)
                .setCancelable(false).create();
        alertDialog.setView(getLayoutInflater().inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.alarm_alert_dialog_layout, null));
        alertDialog.setCustomTitle(getLayoutInflater().inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.alarm_alert_dialog_title, null));
        alertDialog.show();

        ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker))
                .setCurrentHour(sharedPreferences.getInt(Constants.ALARM_HOUR, 0));
        ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker))
                .setCurrentMinute(sharedPreferences.getInt(Constants.ALARM_MINUTE, 0));
        ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker))
                .setIs24HourView(DateFormat.is24HourFormat(this));
        final Switch alarmSwitch = (Switch) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.alarmSwitch);
        alarmSwitch.setChecked(sharedPreferences.getBoolean(Constants.ALARM_IS_ACTIVE, false));

        alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker).setEnabled(alarmSwitch.isChecked());
        alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.alarmSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker).setEnabled(alarmSwitch.isChecked());
            }
        });
        Intent intent = new Intent(this, alarmReceiver.class);
        final PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        final Context context = this;
        alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.set_alarm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (alarmSwitch.isChecked()) {
                    alarmManager.cancel(alarmPendingIntent);

                    editor.putInt(Constants.ALARM_HOUR, ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker)).getCurrentHour());
                    editor.putInt(Constants.ALARM_MINUTE, ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker)).getCurrentMinute());
                    editor.putBoolean(Constants.ALARM_IS_ACTIVE, true);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker)).getCurrentHour());
                    calendar.set(Calendar.MINUTE, ((TimePicker) alertDialog.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timePicker)).getCurrentMinute());
                    calendar.set(Calendar.SECOND, 0);

                    if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmPendingIntent);
                    Toast.makeText(context, "Alarm set!", Toast.LENGTH_SHORT).show();
//                        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),3000,alarmPendingIntent );
                } else {
                    editor.putBoolean(Constants.ALARM_IS_ACTIVE, false);
                    alarmManager.cancel(alarmPendingIntent);
                    Toast.makeText(context, "Alarm canceled", Toast.LENGTH_SHORT).show();
                }
                editor.apply();
                alertDialog.dismiss();
            }
        });
    }

    private void switchFragments(final Fragment fragment) {
        final String fragName = fragment.getClass().getName();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        //checks if all options are false while the user leaves the settings fragment
        //and sets up a dialog box
        if (mFragmentStack.peek().matches(NotificationSettingsFragment.class.getName())
                && !sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true)
                && !sharedPreferences.getBoolean(Constants.OPTION_RAIN, true)
                && !sharedPreferences.getBoolean(Constants.OPTION_WIND, true)
                && !sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true)
                ) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(Constants.NO_OPTION_SELECTED_TITLE);
            alertDialog.setMessage(Constants.NO_OPTION_SELECTED_TEXT);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Constants.GOT_IT, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fragmentTransactionMaker(fragment, fragName, fragmentManager, transaction);
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, Constants.GO_BACK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } else {
            fragmentTransactionMaker(fragment, fragName, fragmentManager, transaction);
        }
    }

    private void fragmentTransactionMaker(Fragment fragment, String fragName, FragmentManager fragmentManager, FragmentTransaction transaction) {
        if (!mFragmentStack.contains(fragName))

        {
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.hide(fragmentManager.findFragmentByTag(mFragmentStack.peek()));
            transaction.add(com.finalProject.dmitroLer.weathernotifier.R.id.fragment_container, fragment, fragName);
            transaction.addToBackStack(fragName);
            mFragmentStack.add(fragName);
        } else

        {
            if (!mFragmentStack.peek().equals(fragName)) {
                transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                transaction.addToBackStack(fragName);
            }
            transaction.hide(fragmentManager.findFragmentByTag(mFragmentStack.peek()));
            mFragmentStack.add(mFragmentStack.remove(mFragmentStack.indexOf(fragName)));
            transaction.show(fragment);
        }
        transaction.commit();
    }


    public Location getLastKnownLocation() {
        return mLastLocation;
    }

    public Address getLastKnownAddress() {
        return mAddress;
    }

    private void displayAddressOutput(String locality) {
        ((TextView) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.location_value)).setText(locality);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(mFragmentStack.peek());
        if (fragment instanceof MainFragment) {
            if (!backWasPressed) {
                Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                backWasPressed = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        backWasPressed = false;
                    }
                }).start();

            } else {
                sharedPreferences.edit().putBoolean(Constants.EXIT_WITH_BACK_BUTTON, true).apply();
                finish();
            }
        } else {
            if (fragment instanceof NotificationSettingsFragment) {
                if (!sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true)
                        && !sharedPreferences.getBoolean(Constants.OPTION_RAIN, true)
                        && !sharedPreferences.getBoolean(Constants.OPTION_WIND, true)
                        && !sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true)
                        ) {
                    final android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
                    alertDialog.setTitle("No option selected");
                    alertDialog.setMessage("If you don't select any option, Weather Notifier wont sent you notifications");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Got it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.popBackStack(MainFragment.class.getName(), 0);
                            mFragmentStack.clear();
                            mFragmentStack.add(mainFragment.getClass().getName());
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Go back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(MainFragment.class.getName(), 0);
                    mFragmentStack.clear();
                    mFragmentStack.add(mainFragment.getClass().getName());
                }
            }
            else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack(MainFragment.class.getName(), 0);
                mFragmentStack.clear();
                mFragmentStack.add(mainFragment.getClass().getName());
            }
        }
    }

    protected void startFetchAddressIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    public void onListFragmentInteraction(RecyclerItems.RecyclerItem item) {
        RecyclerItems.ITEMS.remove(item);
        RecyclerItems.ITEM_MAP.remove(item.id);
        addressHashMap.remove(item.id);
        int itemRemoved = Integer.parseInt(item.id);
        for (int i = itemRemoved; i <= RecyclerItems.ITEM_MAP.size(); i++) {
            RecyclerItems.ITEM_MAP.put(String.valueOf(i), RecyclerItems.ITEM_MAP.remove(String.valueOf(i + 1)));
            addressHashMap.put(String.valueOf(i), addressHashMap.remove(String.valueOf(i + 1)));
        }
    }
}