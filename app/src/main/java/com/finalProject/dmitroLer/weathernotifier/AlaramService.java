package com.finalProject.dmitroLer.weathernotifier;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class AlaramService extends Service {
    private final String TAG = AlaramService.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Constants.START_N_ALARAM_TONE)){
             Log.d(TAG,"Alarm start");
             startAlarmTone();
        }
        else if(action.equals(Constants.STOP_N_ALARAM_TONE)){
            Log.d(TAG,"Alarm stop");
            stopAlarmTone();
            if(!Constants.isVisible){
                Intent resultIntent  =  new Intent(getApplicationContext(),MainActivity.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(resultIntent);
            }
         }

        }
    };
private void startAlarmTone(){
    try {
        Uri notification  =  RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
       if(mediaPlayer == null){
           mediaPlayer = new MediaPlayer();
       }
       mediaPlayer.setDataSource(this,notification);
       mediaPlayer.prepare();
       mediaPlayer.setLooping(true);
       mediaPlayer.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void stopAlarmTone(){
    if(mediaPlayer != null){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer = null;
        }
    }
}
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.STOP_N_ALARAM_TONE);
        intentFilter.addAction(Constants.START_N_ALARAM_TONE);
        intentFilter.addAction(Constants.SEND_MONITOR_DATA_INTENT);
        registerReceiver(broadcastReceiver,intentFilter);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
