package com.finalProject.dmitroLer.weathernotifier;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormatSymbols;
import java.util.Set;

import static android.app.Activity.RESULT_OK;


public class BluetoothFragment extends Fragment{
    private static final String TAG = "MonitorFragment";
    private ImageView connectionImageView, battery_image_view;

    private TextView UVPercentDoneTextView,hiIndexTextView,UVIndexValueTextView,UVTimeLeftTextView,tempValueTextView,humidityValueTextView;
    private Button monitorButton;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> bondedDevicesAdapter;
    private String currentMacAdress=null;
    private View currentView=null;
    private ProgressBar hiIndexdoneProgressBar,UVIndexDoneProgress,UVIndexValueProgress,temperatureValueProgress,humidityValueProgress;
    private StringBuilder dataStringBuilder;
    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Constants.DATA)){
                String info=intent.getStringExtra("DATA");
                    parseAndProcessData(info);
                    Log.d(TAG,info);
            }
            else if(intent.getAction().equals(Constants.UNABLE_TO_CONNECT)){
                String data=intent.getStringExtra("DATA");
                showMsg(data);
            }
            else if(intent.getAction().equals(Constants.CONNECTED)){
                showMsg("Connected successfully...");
                connectionImageView.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.connected_icon);
                if(Constants.lastBatteryStatus>=8)
                    battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon4);
                if(Constants.lastBatteryStatus>=7 && Constants.lastBatteryStatus<8)
                    battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon3);
                if(Constants.lastBatteryStatus>=6 && Constants.lastBatteryStatus<7)
                    battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon2);
                if(Constants.lastBatteryStatus<6)
                    battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon1);
            }
            else if(intent.getAction().equals(Constants.DISCONNECTED)){
                Constants.currentMonitorSate="Monitor";
                monitorButton.setText( Constants.currentMonitorSate);
                hiIndexdoneProgressBar.setProgress(0);
                UVIndexValueProgress.setProgress(0);
                temperatureValueProgress.setProgress(0);
                humidityValueProgress.setProgress(0);
                UVIndexDoneProgress.setProgress(0);
                UVPercentDoneTextView.setText("0"+ Constants.PERCENT);
                UVIndexValueTextView.setText("-");
                tempValueTextView.setText("-");
                humidityValueTextView.setText("-");
                UVTimeLeftTextView.setText("00:00");
                hiIndexTextView.setText("00:00");

                tempValueTextView.setText("-");
                humidityValueTextView.setText("-");
                showMsg("Disconnected...");
                connectionImageView.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.disconnected_icon);
                battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_no_icon);
                ConnectionService.write("8#".getBytes());
            }
            else if(intent.getAction().equals(Constants.RESET_VALUES_INTENT)){
                Constants.currentMonitorSate="Monitor";
                monitorButton.setText( Constants.currentMonitorSate);
                hiIndexdoneProgressBar.setProgress(0);
                UVIndexValueProgress.setProgress(0);
                temperatureValueProgress.setProgress(0);
                humidityValueProgress.setProgress(0);
                UVIndexDoneProgress.setProgress(0);
                UVPercentDoneTextView.setText("0"+ Constants.PERCENT);
                UVIndexValueTextView.setText("-");
                UVTimeLeftTextView.setText("00:00");
                hiIndexTextView.setText("00:00");
                tempValueTextView.setText("-");
                humidityValueTextView.setText("-");
            }
        }
    };
    public void parseAndProcessData(String data){
        try {
            String[] splitedData = data.split(",");
            int UVTimeLeft = Integer.parseInt(splitedData[0]);
            int UVPercentDone=Integer.parseInt(splitedData[1]);
            int UVIndex=Integer.parseInt(splitedData[2]);
            int UVFlag=Integer.parseInt(splitedData[3]);
            int HiTime=Integer.parseInt(splitedData[4]);
            int HiPercentDone=Integer.parseInt(splitedData[5]);
            int HiIndex=Integer.parseInt(splitedData[6]);
            int HiFlag=Integer.parseInt(splitedData[7]);
            int humidity=Integer.parseInt(splitedData[8]);
            int temperature=Integer.parseInt(splitedData[9]);
            int batteryStatus=Integer.parseInt(splitedData[10]);
            int faultySensorFlag=Integer.parseInt(splitedData[11]);
            int coverFlag=Integer.parseInt(splitedData[12]);


            if(UVTimeLeft>720)
                UVTimeLeftTextView.setText(DecimalFormatSymbols.getInstance().getInfinity());
            else {
                UVTimeLeftTextView.setText(convertMinToHourAndMin(UVTimeLeft));
                hiIndexTextView.setText(convertMinToHourAndMin(HiTime));
            }

            UVIndexDoneProgress.setProgress(Integer.parseInt(splitedData[1]));
            UVIndexValueTextView.setText(Integer.toString(UVIndex));
            if(UVPercentDone>100){
                UVPercentDoneTextView.setText(Integer.toString(100)+ Constants.PERCENT);
            }
            else  {UVPercentDoneTextView.setText(splitedData[1]+ Constants.PERCENT);}
            hiIndexdoneProgressBar.setProgress(HiPercentDone);
            UVIndexValueProgress.setProgress(UVIndex);
            temperatureValueProgress.setProgress(temperature);
            humidityValueProgress.setProgress(humidity);
            //hiIndexTextView.setText(splitedData[6]);

            tempValueTextView.setText(""+Integer.toString(temperature)+ Constants.DEGREE);
            humidityValueTextView.setText(""+Integer.toString(humidity)+"%");
            Log.d(TAG,"Humidity value is "+ Integer.toString(humidity));

              if(HiFlag==1){
                  //notifyUser("NOTIFICATION !","Intake water you are exposed to the extreme thermal condition to much time");
                  Log.d(TAG,"HI flag recived");
              }
              if(coverFlag==1){
                  //notifyUser("Sensor Covered","");//TO-DO think how to present the message
                  Log.d(TAG,"Covered flag recived");
              }
              if(UVFlag==1){
                  //notifyUser("ALARM - UVDetection","You have exceeded the daily amount of radiation.Please prevent further exposure to radiation today.");
                  Log.d(TAG,"Alaram and Ringtone service started");
              }
              if(faultySensorFlag==1){
                  //notifyUser("Sensor fault occurred","There is a fault at the hardware site in sensor area,please have a look on it");
                  Log.d(TAG,"Alaram and Ringtone service started");
              }

            Constants.lastUVTimeLeft = UVTimeLeft;
            Constants.lastUVPercentDone = UVPercentDone;
            Constants.lastUVIndex = UVIndex;
            Constants.lastHITimeLeft = HiTime;
            Constants.lastHiPercentDone = HiPercentDone;
            Constants.lastHiIndex = HiIndex;
            Constants.lastTemperature = temperature;
            Constants.lastHumidity = humidity;
            Constants.lastBatteryStatus = batteryStatus;

        }
        catch (Exception e){
            e.printStackTrace();
            showMsg("Error while processing data...");
        }


    }
    private String convertMinToHourAndMin(int min){
        int hours=(min/60);
        int minutes=(min%60);
        //long hours = TimeUnit.MINUTES.toHours(120);
       // Log.d(TAG,"Hour is :"+Long.toString(hours));
       // long remainMinute = min - TimeUnit.HOURS.toMinutes(hours);
       // Log.d(TAG,"Remain min is :"+Long.toString(remainMinute));
        String result = String.format("%02d", hours) + ":" + String.format("%02d",minutes);
        return result;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            Toast.makeText(getActivity(),"Your Device does't support bluetooth connection",Toast.LENGTH_SHORT).show();
            return ;
        }
        if(!mBluetoothAdapter.isEnabled()){
           Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
       View rootView = inflater.inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.fragment_bluetooth, container, false);
        connectionImageView =(ImageView)rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.connection_image_view);
        battery_image_view =(ImageView)rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.battery_image_view);
        monitorButton =(Button)rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.monitor_button);
        tempValueTextView = (TextView) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.temperature_valuetext_view);
        humidityValueTextView = (TextView) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.humidity_valuetext_view);
        UVPercentDoneTextView =(TextView)rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.UV_percent_done_text_view);
        hiIndexTextView = (TextView) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.water_intake_index_text_view);
        UVIndexDoneProgress =(ProgressBar)rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.uv_progress_done_progress);
        hiIndexdoneProgressBar = (ProgressBar) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.hi_index_percent_done_progress);
        UVIndexValueProgress = (ProgressBar) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.uvindex_percent_done_progress);
        temperatureValueProgress = (ProgressBar) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.temperature_index_percent_done_progress);
        humidityValueProgress = (ProgressBar) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.humidity_index_percent_done_progress);
        UVIndexValueTextView  = (TextView) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.uv_index_text_view);
        UVTimeLeftTextView = (TextView) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.UV_time_left_text_view);
        UVIndexDoneProgress.setMax(100);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        FloatingActionButton floatingActionButton=(FloatingActionButton)rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              showSunscreenDialog();
            }
        });
        connectionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter.isEnabled())
                        showPairedDevicesDialogu(listBondedDevices());
                    else
                        Toast.makeText(getActivity(), "Check bluetooth setting...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(monitorButton.getText().toString().equals("Stop Monitor")){
                    //
                    Constants.currentMonitorSate="Monitor";
                    ConnectionService.write("8#".getBytes());
                    monitorButton.setText(Constants.currentMonitorSate);
                    hiIndexdoneProgressBar.setProgress(0);
                    UVIndexDoneProgress.setProgress(0);
                    UVIndexValueProgress.setProgress(0);
                    temperatureValueProgress.setProgress(0);
                    humidityValueProgress.setProgress(0);
                    UVPercentDoneTextView.setText("0"+ Constants.PERCENT);
                    UVIndexValueTextView.setText("-");
                    UVTimeLeftTextView.setText("00:00");
                    hiIndexTextView.setText("00:00");
                    tempValueTextView.setText("-");
                    humidityValueTextView.setText("-");
                    return;
                }
                if(mBluetoothAdapter.isEnabled()&& Constants.isConnnected)
                {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                    Constants.ageOfUser = sharedPreferences.getInt(Constants.AGE,0);
                    Constants.weightOfUSer = sharedPreferences.getInt(Constants.WEIGHT,0);
                    Constants.UV_Skin_MED_Max = sharedPreferences.getInt(Constants.SKIN_COLOR,0);
                    Constants.resetHiAlarmFlag = 0;
                    Constants.SPF_factor = Double.parseDouble(sharedPreferences.getString("SPF_FACTOR","0"));
                    Constants.motorControlFlag = 0;
                    Constants.coverControlFlag = 0;
                    Constants.arduinoStatusFlag = 1;
                    Constants.clothType = sharedPreferences.getInt(Constants.CLOTH,0);
                 dataStringBuilder
                         .append(Constants.arduinoStatusFlag)
                         .append(',')
                         .append(Constants.ageOfUser)
                         .append(',')
                         .append(Constants.weightOfUSer)
                         .append(',')
                         .append(Constants.UV_Skin_MED_Max)
                         .append(',')
                         .append(Constants.resetHiAlarmFlag)
                         .append(',')
                         .append(Constants.SPF_factor)
                         .append(',')
                         .append(Constants.motorControlFlag)
                         .append(',')
                         .append(Constants.coverControlFlag)
                         .append(',')
                         .append(Constants.clothType)
                         .append('#');
                 ConnectionService.write(dataStringBuilder.toString().getBytes());
                 dataStringBuilder.delete(0,dataStringBuilder.length());
                 Constants.currentMonitorSate="Stop Monitor";
                 monitorButton.setText(Constants.currentMonitorSate);
                }
                else {
                    showMsg("Check bluetooth setting.");
                }
            }
        });
        bondedDevicesAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction( Constants.CONNECTED);
        intentFilter.addAction(Constants.DISCONNECTED);
        intentFilter.addAction(Constants.DATA);
        intentFilter.addAction(Constants.UNABLE_TO_CONNECT);
        intentFilter.addAction(Constants.RESET_VALUES_INTENT);
        getActivity().registerReceiver(mReceiver,intentFilter);
        dataStringBuilder = new StringBuilder();
        monitorButton.setText(Constants.currentMonitorSate);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        monitorButton.setText(Constants.currentMonitorSate);
        hiIndexdoneProgressBar.setProgress(Constants.lastHiPercentDone);
        UVIndexValueProgress.setProgress(Constants.lastUVIndex);
        temperatureValueProgress.setProgress(Constants.lastTemperature);
        humidityValueProgress.setProgress(Constants.lastHumidity);
        UVIndexDoneProgress.setProgress(Constants.lastUVPercentDone);
        UVPercentDoneTextView.setText(Integer.toString(Constants.lastUVPercentDone)+ Constants.PERCENT);
        UVIndexValueTextView.setText(""+Integer.toString(Constants.lastUVIndex));
        UVTimeLeftTextView.setText(convertMinToHourAndMin(Constants.lastUVTimeLeft));
        hiIndexTextView.setText(convertMinToHourAndMin(Constants.lastHITimeLeft));
        tempValueTextView.setText(""+Integer.toString(Constants.lastTemperature)+ Constants.DEGREE);
        humidityValueTextView.setText(""+Constants.lastHumidity+ Constants.PERCENT);
    }

    private void showSunscreenDialog(){
        String names[] ={"No Sun Screen","SPF 15-30","SPF 30-50","SPF 50-70","SPF 70+"};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences=getActivity().getSharedPreferences(MainActivity.class.getSimpleName(),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                switch (which){
                    case 0:
                        Constants.SPF_factor = 1;
                        editor.putString("SPF_FACTOR",Double.toString(Constants.SPF_factor)).apply();
                        showMsg("No sun screen selected");
                        break;
                    case 1:
                        Constants.SPF_factor = 15;
                        editor.putString("SPF_FACTOR",Double.toString(Constants.SPF_factor)).apply();
                        showMsg("SPF 15-30 selected");
                        break;
                    case 2:
                        Constants.SPF_factor = 30;
                        editor.putString("SPF_FACTOR",Double.toString(Constants.SPF_factor)).apply();
                        showMsg("SPF 30-50 selected");
                        break;
                    case 3:
                        Constants.SPF_factor = 50;
                        editor.putString("SPF_FACTOR",Double.toString(Constants.SPF_factor)).apply();
                        showMsg("SPF 50-70 selected");
                        break;
                    case 4:
                        Constants.SPF_factor = 70;
                        editor.putString("SPF_FACTOR",Double.toString(Constants.SPF_factor)).apply();
                        showMsg("SPF 70+ selected");
                        break;
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
                Dialog dialog = alertDialog.create();
                dialog.setCancelable(false);
                dialog.setTitle("Select sun screen");
                dialog.show();
    }
    private void showPairedDevicesDialogu(ArrayAdapter<String> adapter){
        View view = getActivity().getLayoutInflater().inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.custom_bonded_device_and_connection_dilogue,null);
        ListView listView = (ListView)view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.bonded_device_list_view);
        Button cancelButton = (Button)view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.dilogue_cancel_button);
        Button connectButton = (Button)view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.dilogue_connect_button);
        listView.setAdapter(adapter);
        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentView!=null){
                    if(view!=currentView){
                        currentView.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
                TextView text = (TextView)view;
                String data = text.getText().toString().trim();
                currentMacAdress = data.substring((data.length()-17),data.length());
                text.setBackgroundColor(getResources().getColor(com.finalProject.dmitroLer.weathernotifier.R.color.bt_back));
                currentView = view;
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog.isShowing())dialog.dismiss();

            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constants.isConnnected){
                    showMsg("Already connected...");
                    return;
                }
                if(currentMacAdress==null){
                    Toast.makeText(getActivity(),"Please select device from the list",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(dialog.isShowing()){
                    dialog.dismiss();
                }

                Intent intent = new Intent(getActivity(),ConnectionService.class);
                intent.putExtra("MAC",currentMacAdress.trim());
                intent.setAction("CONNECT");
                getActivity().startService(intent);
                getActivity().startService(new Intent(getActivity(),AlaramService.class));

                showMsg("Please wait while we connect you...");
            }
        });
    }
    private ArrayAdapter<String> listBondedDevices(){
        Log.d(TAG,"paired devices method called");
        pairedDevices=mBluetoothAdapter.getBondedDevices();
        if(bondedDevicesAdapter.getCount()>0){
            bondedDevicesAdapter.clear();
        }
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            if(bondedDevicesAdapter.getCount()>0){
                bondedDevicesAdapter.clear();
            }
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                bondedDevicesAdapter.add(deviceName+"\n"+deviceHardwareAddress);
            }
            Log.d(TAG,"we have some paired devices");
        }
        else {
            bondedDevicesAdapter.add("No paired Devices yet");
        }
        return bondedDevicesAdapter;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(resultCode == RESULT_OK){
                    Toast.makeText(getActivity(),"Bluetooth on",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(),"Leaving you,you should turm on bluetooth to use the app",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
      //  LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        getActivity().unregisterReceiver(mReceiver);
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume(); // bat-TAG
        if(Constants.isConnnected){
            connectionImageView.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.connected_icon);
            if(Constants.lastBatteryStatus>=8)
                battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon4);
            if(Constants.lastBatteryStatus>=7 && Constants.lastBatteryStatus<8)
                battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon3);
            if(Constants.lastBatteryStatus>=6 && Constants.lastBatteryStatus<7)
                battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon2);
            if(Constants.lastBatteryStatus<6)
                battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_icon1);

        }
        else {
            connectionImageView.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.disconnected_icon);
            battery_image_view.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.mipmap.battery_no_icon);
        }

    }
    private void showMsg(String msg){
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }
}
