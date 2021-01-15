package com.project.yamayachi;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class BackgroundSoundService extends Service {
    private final IBinder binder = new LocalBinder();
    MediaPlayer player;
    boolean pause = true;

    public IBinder onBind(Intent arg0) {
        return binder;
    }

    public IBinder onUnBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        player.setVolume(100, 100);
        //player.start();
    }


    public void setVolume(float volume){
        player.setVolume(volume, volume);
    }

    public void stopMusic() {
        if(player!=null && pause) {
            try {
                if (player.isPlaying()) {
                    player.pause();

                }
        } catch (Exception e) {
            Log.w(BackgroundSoundService.class.getName(), String.format("Failed to stop media player: %s", e));
        }
        }
        pause = true;
    }
    public void startMusic(){
        if(player!=null)
            player.start();
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
    }

    public class LocalBinder extends Binder {
        BackgroundSoundService getService() {
            return BackgroundSoundService.this;
        }
    }
}
