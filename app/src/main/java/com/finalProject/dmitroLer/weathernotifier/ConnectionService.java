package com.finalProject.dmitroLer.weathernotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectionService extends Service{
    private final String TAG="ConnectionService";
  //  private LocalBroadcastManager localBroadcastManager;
    private BluetoothAdapter mBluetoothAdapter;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private  static InputStream mmInStream;
    private  static OutputStream mmOutStream;
    public static String titlee;
    public static String infoo;
    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(Constants.DATA)){
                String info=intent.getStringExtra("DATA");
                parseAndProcessData(info);
                Log.d(TAG,"Data send for processing");
            }
            else if(action.equals(Constants.SEND_MONITOR_DATA_INTENT)){
               // sendMonitorData();
              try{  write("7#".getBytes());}
              catch (Exception e){
                  e.printStackTrace();
              }
                Log.d(TAG,"Monitor data send");
            }
        }
    };
    private void parseAndProcessData(String data){
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
            int temperature=Integer.parseInt(splitedData[8]);
            int humidity=Integer.parseInt(splitedData[9]);
            int batteryStatus=Integer.parseInt(splitedData[10]);
            int faultySensorFlag=Integer.parseInt(splitedData[11]);
            int coverFlag=Integer.parseInt(splitedData[12]);
            if(HiFlag==1){
               switch (HiIndex) {
                   case 1:
                       notifyUser("NOTIFICATION !", "Intake water you are exposed to thermal condition to much time");
                       write("3#".getBytes());
                       break;
                   case 2:
                       notifyUser("NOTIFICATION !", "Intake water you are exposed to high thermal condition to much time\n" +
                               "Fatigue is possible with prolonged exposure and activity");
                       write("3#".getBytes());
                       break;
                   case 3:
                       notifyUser("NOTIFICATION !", "Intake 1 liter water per hour you are exposed to high thermal condition to much time\n" +
                               " Heat cramps and heat exhaustion are possible");
                       write("3#".getBytes());
                       break;
                   case 4:
                       notifyUser("NOTIFICATION !", "Intake 1.5 liter water per hour, you are exposed to extreme thermal condition to much time\n" +
                               "Heat cramps and heat exhaustion are likely");
                       write("3#".getBytes());
                       break;
                   case 5:
                       notifyUser("NOTIFICATION !", "Intake 2 liter water per hour,  you are exposed to extreme thermal condition to much time\n" +
                               "Heat stroke or sunstroke highly likely");
                       write("3#".getBytes());
                       break;
               }
            }
            if(coverFlag==1){
                notifyUser("Sensor Covered","The UV sensor is covered, please check the device");
                write("5#".getBytes());
            }
            if(UVFlag==1){
                notifyUser("ALARM - UVDetection","You have exceeded the daily amount of radiation.Please prevent further exposure to radiation today.");
                Log.d(TAG,"Alaram and Ringtone service started");
                write("4#".getBytes());
            }
            if(faultySensorFlag==1){
                //There is a fault at the hardware site in sensor area,please have a look on it
                notifyUser("Sensor fault occurred","Probability for fault at hardware detected. Please contact the service");
                Log.d(TAG,"Alaram and Ringtone service started");
                write("6#".getBytes());
            }
            if(HiFlag==0 && coverFlag==0 && UVFlag==0 && faultySensorFlag==0){
                write("2#".getBytes());
            }

        }
        catch (Exception e){
            e.printStackTrace();
           // showMsg("Error while processing data...");
        }
    }
    private void notifyUser(String title,String info){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(info);
        if(title.equals("NOTIFICATION !")){
            if(Constants.isVisible){
                Intent resultIntent=new Intent(Constants.DO_NOTHING_INTENT);
                PendingIntent resultPendingIntent = PendingIntent.getBroadcast(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                mBuilder.setAutoCancel(true);
                mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent);
                mBuilder.setContentIntent(resultPendingIntent);
                int mNotificationId =(int)System.currentTimeMillis();
                NotificationManager mNotifyMgr =(NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
                Notification notification= mBuilder.build();
                notification.flags|=Notification.FLAG_AUTO_CANCEL;
                mNotifyMgr.notify(mNotificationId,  notification);
                return;
            }
            Intent resultIntent=new Intent(Constants.STOP_N_ALARAM_TONE);
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mBuilder.setAutoCancel(true);
            mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent);
            mBuilder.setContentIntent(resultPendingIntent);
            int mNotificationId =(int)System.currentTimeMillis();
            NotificationManager mNotifyMgr =(NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
            Notification notification= mBuilder.build();
            notification.flags|=Notification.FLAG_AUTO_CANCEL;
            mNotifyMgr.notify(mNotificationId,  notification);
            Log.d("TAG","Inside notification builder");
            Constants.isToShowAlert=false;
        }
        else if(title.equals("Sensor Covered")){
            titlee=title;
            infoo=info;
            if(Constants.isVisible){
                sendBroadcast(new Intent(Constants.ALERT_DILOGE_INTENT));
                return;
            }
            Intent resultIntent=new Intent(Constants.STOP_N_ALARAM_TONE);
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mBuilder.setAutoCancel(true);
            mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent);
            mBuilder.setContentIntent(resultPendingIntent);
            int mNotificationId =(int)System.currentTimeMillis();
            NotificationManager mNotifyMgr =(NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            Notification notification= mBuilder.build();
            notification.flags|=Notification.FLAG_AUTO_CANCEL;
            mNotifyMgr.notify(mNotificationId,  notification);
            Log.d("TAG ","Inside notification builder sensor covered");
            Constants.isToShowAlert=true;
        }
        else if(title.equals("ALARM - UVDetection")){
            titlee=title;
            infoo=info;
           if(Constants.isVisible){
               sendBroadcast(new Intent(Constants.START_N_ALARAM_TONE));
               sendBroadcast(new Intent(Constants.ALERT_DILOGE_INTENT));
               return;
           }
            Intent resultIntent1 = new Intent(Constants.STOP_N_ALARAM_TONE);
           // Intent resultIntent1=new Intent(this,MainActivity.class);
           // resultIntent1.putExtra("DATA","ALARM_UV_DETECTION");
            PendingIntent resultPendingIntent1 = PendingIntent.getBroadcast(this, 0, resultIntent1, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(resultPendingIntent1);
            mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent1);
            mBuilder.setContentIntent(resultPendingIntent1);
            int mNotificationId =(int)System.currentTimeMillis();
            NotificationManager mNotifyMgr =(NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            Notification notification= mBuilder.build();
            notification.flags|=Notification.FLAG_AUTO_CANCEL;
            mNotifyMgr.notify(mNotificationId,  notification);
            Log.d("TAG","Inside notification builder");

           this.sendBroadcast(new Intent(Constants.START_N_ALARAM_TONE));
           Constants.isToShowAlert=true;
        }
        else if(title.equals("Sensor fault occurred")){
            titlee=title;
            infoo=info;
            if(Constants.isVisible){
                sendBroadcast(new Intent(Constants.START_N_ALARAM_TONE));
                sendBroadcast(new Intent(Constants.ALERT_DILOGE_INTENT));
                return;
            }
            Intent resultIntent1 = new Intent(Constants.STOP_N_ALARAM_TONE);
            resultIntent1.putExtra("DATA","SENSOR_FAULT_OCCURRED");
            PendingIntent resultPendingIntent1 = PendingIntent.getBroadcast(this, 0, resultIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent1);
            mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent1);
            int mNotificationId =(int)System.currentTimeMillis();
            NotificationManager mNotifyMgr =(NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            Notification notification= mBuilder.build();
            notification.flags|=Notification.FLAG_AUTO_CANCEL;
            mNotifyMgr.notify(mNotificationId,  notification);
            Log.d("TAG","Inside notification builder");

            this.sendBroadcast(new Intent(Constants.START_N_ALARAM_TONE));
            Constants.isToShowAlert=true;
        }
        else if(title.equals("NOTIFICATION")){
            titlee=title;
            infoo=info;
            if(Constants.isVisible){
                Intent resultIntent=new Intent(Constants.DO_NOTHING_INTENT);
                PendingIntent resultPendingIntent = PendingIntent.getBroadcast(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                mBuilder.setAutoCancel(true);
                mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent);
                mBuilder.setContentIntent(resultPendingIntent);
                int mNotificationId =(int)System.currentTimeMillis();
                NotificationManager mNotifyMgr =(NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
                Notification notification= mBuilder.build();
                notification.flags|=Notification.FLAG_AUTO_CANCEL;
                mNotifyMgr.notify(mNotificationId,  notification);
                sendBroadcast(new Intent(Constants.ALERT_DILOGE_INTENT));
                return;
            }
            Intent resultIntent=new Intent(Constants.STOP_N_ALARAM_TONE);
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mBuilder.setAutoCancel(true);
            mBuilder.addAction(com.finalProject.dmitroLer.weathernotifier.R.mipmap.ic_launcher,"OK",resultPendingIntent);
            mBuilder.setContentIntent(resultPendingIntent);
            int mNotificationId =(int)System.currentTimeMillis();
            NotificationManager mNotifyMgr =(NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
            Notification notification= mBuilder.build();
            notification.flags|=Notification.FLAG_AUTO_CANCEL;
            mNotifyMgr.notify(mNotificationId,  notification);
            Log.d("TAG","Inside notification builder");
            Constants.isToShowAlert=false;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
       // localBroadcastManager=LocalBroadcastManager.getInstance(this);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(Constants.START_N_ALARAM_TONE);
        intentFilter.addAction(Constants.STOP_N_ALARAM_TONE);
        intentFilter.addAction(Constants.SEND_MONITOR_DATA_INTENT);
        intentFilter.addAction(Constants.DATA);
        registerReceiver(mReceiver,intentFilter);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"OnStartCommand called");
        String action=intent.getAction();
        assert action != null;
        switch (action){
            case "CONNECT":
                String MAC=intent.getStringExtra("MAC").trim();
                BluetoothDevice device=mBluetoothAdapter.getRemoteDevice(MAC);
                ConnectThread connectThread=new ConnectThread(device);
                connectThread.start();
                Log.d(TAG,"Connecting...");
                break;
        }
        if(action.equals(Constants.SEND_MONITOR_DATA_INTENT)){
          //  Log.d(TAG,"Do nothing intent called in connection service");
        }
        return START_NOT_STICKY;
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Constants.isConnnected=false;
                    Intent intent=new Intent();
                    intent.setAction( Constants.UNABLE_TO_CONNECT);
                    intent.putExtra("DATA","Unable to connect...");
                   // localBroadcastManager.sendBroadcast(intent);
                    sendBroadcast(intent);
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //  manageMyConnectedSocket(mmSocket);
            ManageSocket manageSocket=new ManageSocket(mmSocket);
            manageSocket.start();
            Constants.isConnnected=true;
            Intent intent=new Intent();
            intent.setAction( Constants.CONNECTED);
           // localBroadcastManager.sendBroadcast(intent);
            sendBroadcast(intent);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    private class ManageSocket extends Thread {
        private final BluetoothSocket mmSocket;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ManageSocket(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            int numBytes; // bytes returned from read()
            Intent intent=new Intent();
            StringBuilder stringBuilder=new StringBuilder();
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    byte [] mmBuffer = new byte[1024];
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    String data=new String(mmBuffer);
                    if(data.indexOf('#')!=-1){
                        stringBuilder.append(data.trim());
                        String info=stringBuilder.toString().trim();
                        String finalD=info.substring(0,(info.length()-1));
                        intent.putExtra("DATA",finalD);
                        intent.setAction(Constants.DATA);
                       // localBroadcastManager.sendBroadcast(intent);
                        sendBroadcast(intent);
                        Log.d("Total Data:",stringBuilder.toString());
                        stringBuilder.delete(0,stringBuilder.toString().length());
                    }
                    else {
                        stringBuilder.append(data.trim());
                    }

                } catch (IOException e) {
                    intent=new Intent();
                    intent.setAction( Constants.DISCONNECTED);
                  //  localBroadcastManager.sendBroadcast(intent);
                    sendBroadcast(intent);
                    Log.d(TAG, "Input stream was disconnected", e);
                    Constants.isConnnected=false;
                    notifyUser("NOTIFICATION","The Communication lost,please check the device");
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
    }
    public static void write(byte[] bytes) {
        if(mmOutStream==null){
            Log.d("ConnectionService","OutPut stream is null");
            return;
        }
        try {
            Log.d("ConnectionService",new String(bytes)+" Send to remote device");
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("ConnectionService", "Error occurred when sending data", e);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        stopSelf();
        super.onDestroy();
    }
}
