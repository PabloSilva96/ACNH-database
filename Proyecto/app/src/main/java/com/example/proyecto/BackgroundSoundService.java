package com.example.proyecto;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BackgroundSoundService extends Service {
    MediaPlayer player;

    public IBinder onBind(Intent arg0) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();


        player = MediaPlayer.create(this, R.raw.musica);
        player.setLooping(true);
        player.setVolume(50,50);


    }
    public int onStartCommand(Intent intent, int flags, int startId) {

        player.start();

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {

        player.stop();
        player.release();
    }
}